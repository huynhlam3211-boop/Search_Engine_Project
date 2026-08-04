import { BrowserWindow, WebContentsView, type Input, type WebContents } from 'electron'
import { join } from 'path'
import { is } from '@electron-toolkit/utils'

const CHROME_HEIGHT = 122

const SIDE_RAIL_WIDTH = 48

export const HOME_URL = "vnsearch://home"

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
    private readonly tabs = new Map<String, TabEntry>() 
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

        this.createTab(HOME_URL)
    }


    private layoutChrome(): void {

    }

    private layoutTabView(view: WebContentsView): void {

    }

    setPanelWidth(px: number): void {

    }

    setOverlay(active: boolean): void {

    }

    setZoom() {

    }

    private layoutAll():void {

    }

    listTabs(): TabState[] {

    }

    createTab(): string {

    }

    closeTab(): string {
        
    }

    switchTab(): string {
        
    }

    navigate(): string {
        
    }

    goBack(): string {
        
    }

    goForward(): string {
        
    }

    reload(): string {
        
    }

    print(): string {
        
    }

    private forwardShortcuts(): void {
        
    }

    private emit(state: TabState): void {

    }
}