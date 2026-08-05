import { BrowserWindow, WebContentsView, type Input, type WebContents } from 'electron'
import { join } from 'path'
import { is } from '@electron-toolkit/utils'

const CHROME_HEIGHT = 122

const SIDE_RAIL_WIDTH = 48

export const HOME_URL = 'vnsearch://home'

function shortcutName(input: Input): string | null {
  const key = input.key.toLowerCase()

  if (input.control && !input.alt && !input.shift) {
    if (key === 't') return 'newTab'
    if (key === 'w') return 'closeTab'
    if (key === 'l') return 'focusOmnibox'
    if (key === 'd') return 'bookmark'
    if (key === 'r') return 'reload'
  }
  if (input.alt && !input.control) {
    if (key === 'd') return 'focusOmnibox'
    if (key === 'arrowleft') return 'back'
    if (key === 'arrowright') return 'forward'
    if (key === 'home') return 'home'
  }
  if (key === 'f5' && !input.control && !input.alt) {
    return 'reload'
  }
  return null
}

export interface TabState {
  id: string
  url: string
  title: string
  loading: boolean
  canGoBack: boolean
  canGoForward: boolean
}

interface TabEntry {
  state: TabState
  view: WebContentsView | null
}

export class TabManager {
  private readonly window: BrowserWindow
  private readonly chromeView: WebContentsView
  private readonly tabs = new Map<string, TabEntry>()
  private order: string[] = []
  private activeTabId: string | null = null
  private nextTabId = 1
  private panelWidth = 0
  private overlay = false

  constructor(window: BrowserWindow) {
    this.window = window
    this.chromeView = new WebContentsView({
      webPreferences: {
        preload: join(__dirname, '../preload/index.js'),
        contextIsolation: true,
        nodeIntegration: false,
        sandbox: false
      }
    })
    this.window.contentView.addChildView(this.chromeView)
    this.layoutChrome()

    // Giao dien React nam trong chromeView chu khong phai trong BrowserWindow:
    // BrowserWindow o day chi con la cai khung rong chua cac WebContentsView.
    if (is.dev && process.env['ELECTRON_RENDERER_URL']) {
      this.chromeView.webContents.loadURL(process.env['ELECTRON_RENDERER_URL'])
    } else {
      this.chromeView.webContents.loadFile(join(__dirname, '../renderer/index.html'))
    }

    this.window.on('resize', () => this.layoutAll())

    this.createTab(HOME_URL)
  }

  /** Cho main/index.ts doi giao dien nap xong roi moi hien cua so (tranh nhay trang trang). */
  onChromeReady(callback: () => void): void {
    this.chromeView.webContents.once('did-finish-load', callback)
  }

  get chromeContents(): WebContents {
    return this.chromeView.webContents
  }

  private layoutChrome(): void {
    const { width, height } = this.window.getContentBounds()
    this.chromeView.setBounds({ x: 0, y: 0, width, height })
  }

  private layoutTabView(view: WebContentsView): void {
    const { width, height } = this.window.getContentBounds()
    view.setBounds({
      x: 0,
      y: CHROME_HEIGHT,
      width: Math.max(0, width - SIDE_RAIL_WIDTH - this.panelWidth),
      height: Math.max(0, height - CHROME_HEIGHT)
    })
  }

  setPanelWidth(px: number): void {
    this.panelWidth = Math.max(0, Math.round(px))
    this.layoutAll()
  }

  /**
   * TAM THOI: tab view luon nam DE len tren chromeView, nen menu/dropdown do
   * React ve se bi trang web che mat. Cach re nhat: an tab view khi co overlay.
   */
  setOverlay(active: boolean): void {
    this.overlay = active
    this.layoutAll()
  }

  setZoom(id: string, factor: number): void {
    const entry = this.tabs.get(id)
    entry?.view?.webContents.setZoomFactor(factor)
  }

  private layoutAll(): void {
    this.layoutChrome()
    for (const [id, entry] of this.tabs) {
      if (!entry.view) continue
      const visible = id === this.activeTabId && !this.overlay
      entry.view.setVisible(visible)
      if (visible) this.layoutTabView(entry.view)
    }
  }

  listTabs(): TabState[] {
    return this.order.map((id) => this.tabs.get(id)!.state)
  }

  getActiveTabId(): string | null {
    return this.activeTabId
  }

  createTab(url: string = HOME_URL): string {
    const id = `tab-${this.nextTabId++}`
    this.tabs.set(id, {
      state: {
        id,
        url: HOME_URL,
        title: 'Tab mới',
        loading: false,
        canGoBack: false,
        canGoForward: false
      },
      view: null
    })
    this.order.push(id)
    this.switchTab(id)
    if (url !== HOME_URL) this.navigate(id, url)
    this.emit()
    return id
  }

  closeTab(id: string): void {
    const entry = this.tabs.get(id)
    if (!entry) return

    this.destroyView(entry)
    this.tabs.delete(id)
    const index = this.order.indexOf(id)
    this.order = this.order.filter((t) => t !== id)

    if (this.activeTabId === id) {
      this.activeTabId = null
      const next = this.order[Math.min(index, this.order.length - 1)]
      if (next) this.switchTab(next)
    }
    // Dong tab cuoi cung thi mo lai trang chu thay vi de cua so trong.
    if (this.order.length === 0) this.createTab(HOME_URL)
    this.emit()
  }

  switchTab(id: string): void {
    if (!this.tabs.has(id)) return
    this.activeTabId = id
    this.layoutAll()
    this.emit()
  }

  navigate(id: string, url: string): void {
    const entry = this.tabs.get(id)
    if (!entry) return

    if (url === HOME_URL) {
      // Trang chu la mot man hinh cua React nen khong can WebContentsView.
      this.destroyView(entry)
      entry.state = { ...entry.state, url: HOME_URL, title: 'Tab mới', loading: false }
      this.layoutAll()
      this.emit()
      return
    }

    const target = /^[a-z]+:\/\//i.test(url) ? url : `https://${url}`
    const view = entry.view ?? this.createView(entry)
    entry.state = { ...entry.state, url: target, loading: true }
    this.layoutAll()
    view.webContents.loadURL(target).catch(() => {
      entry.state = { ...entry.state, loading: false, title: 'Không mở được trang' }
      this.emit()
    })
    this.emit()
  }

  goBack(id: string): void {
    const nav = this.tabs.get(id)?.view?.webContents.navigationHistory
    if (nav?.canGoBack()) nav.goBack()
  }

  goForward(id: string): void {
    const nav = this.tabs.get(id)?.view?.webContents.navigationHistory
    if (nav?.canGoForward()) nav.goForward()
  }

  reload(id: string): void {
    this.tabs.get(id)?.view?.webContents.reload()
  }

  print(id: string): void {
    this.tabs.get(id)?.view?.webContents.print({}, () => undefined)
  }

  private createView(entry: TabEntry): WebContentsView {
    const view = new WebContentsView({
      webPreferences: { contextIsolation: true, nodeIntegration: false }
    })
    entry.view = view
    this.window.contentView.addChildView(view)
    this.layoutTabView(view)
    this.bindViewEvents(entry, view)
    this.forwardShortcuts(view.webContents)
    return view
  }

  private destroyView(entry: TabEntry): void {
    if (!entry.view) return
    this.window.contentView.removeChildView(entry.view)
    entry.view.webContents.close()
    entry.view = null
  }

  private bindViewEvents(entry: TabEntry, view: WebContentsView): void {
    const wc = view.webContents
    const sync = (patch: Partial<TabState>): void => {
      entry.state = {
        ...entry.state,
        ...patch,
        canGoBack: wc.navigationHistory.canGoBack(),
        canGoForward: wc.navigationHistory.canGoForward()
      }
      this.emit()
    }

    wc.on('did-start-loading', () => sync({ loading: true }))
    wc.on('did-stop-loading', () => sync({ loading: false }))
    wc.on('page-title-updated', (_e, title) => sync({ title }))
    wc.on('did-navigate', (_e, url) => sync({ url }))
    wc.on('did-navigate-in-page', (_e, url) => sync({ url }))
    wc.setWindowOpenHandler(({ url }) => {
      this.createTab(url)
      return { action: 'deny' }
    })
  }

  private forwardShortcuts(wc: WebContents): void {
    // Khi tieu diem dang o trong trang web, phim tat cua trinh duyet phai duoc
    // chuyen nguoc ve giao dien React, neu khong Ctrl+T se roi vao trang web.
    wc.on('before-input-event', (event, input) => {
      if (input.type !== 'keyDown') return
      const name = shortcutName(input)
      if (!name) return
      event.preventDefault()
      this.chromeView.webContents.send('browser:shortcut', name)
    })
  }

  private emit(): void {
    if (this.chromeView.webContents.isDestroyed()) return
    this.chromeView.webContents.send('browser:tabs', {
      tabs: this.listTabs(),
      activeTabId: this.activeTabId
    })
  }
}
