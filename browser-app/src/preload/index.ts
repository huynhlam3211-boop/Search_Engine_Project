import { contextBridge, ipcRenderer } from 'electron'
import { electronAPI } from '@electron-toolkit/preload'
import type { TabState } from '../main/tabManager'

type TabsPayload = { tabs: TabState[]; activeTabId: string | null }

const browserApi = {
  listTabs: (): Promise<TabsPayload> => ipcRenderer.invoke('browser:listTabs'),
  createTab: (url?: string): Promise<string> => ipcRenderer.invoke('browser:createTab', url),
  closeTab: (id: string): Promise<void> => ipcRenderer.invoke('browser:closeTab', id),
  switchTab: (id: string): Promise<void> => ipcRenderer.invoke('browser:switchTab', id),
  navigate: (id: string, url: string): Promise<void> =>
    ipcRenderer.invoke('browser:navigate', id, url),
  goBack: (id: string): Promise<void> => ipcRenderer.invoke('browser:goBack', id),
  goForward: (id: string): Promise<void> => ipcRenderer.invoke('browser:goForward', id),
  reload: (id: string): Promise<void> => ipcRenderer.invoke('browser:reload', id),
  print: (id: string): Promise<void> => ipcRenderer.invoke('browser:print', id),
  setZoom: (id: string, factor: number): Promise<void> =>
    ipcRenderer.invoke('browser:setZoom', id, factor),

  setPanelWidth: (px: number): void => ipcRenderer.send('browser:setPanelWidth', px),
  setOverlay: (active: boolean): void => ipcRenderer.send('browser:setOverlay', active),

  // Tra ve ham huy dang ky de React goi trong phan cleanup cua useEffect.
  onTabsChanged: (cb: (payload: TabsPayload) => void): (() => void) => {
    const listener = (_e: unknown, payload: TabsPayload): void => cb(payload)
    ipcRenderer.on('browser:tabs', listener)
    return () => ipcRenderer.removeListener('browser:tabs', listener)
  },
  onShortcut: (cb: (name: string) => void): (() => void) => {
    const listener = (_e: unknown, name: string): void => cb(name)
    ipcRenderer.on('browser:shortcut', listener)
    return () => ipcRenderer.removeListener('browser:shortcut', listener)
  }
}

const windowApi = {
  minimize: (): void => ipcRenderer.send('win:minimize'),
  toggleMaximize: (): void => ipcRenderer.send('win:toggleMaximize'),
  close: (): void => ipcRenderer.send('win:close'),
  isMaximized: (): Promise<boolean> => ipcRenderer.invoke('win:isMaximized'),
  onMaximizedChanged: (cb: (maximized: boolean) => void): (() => void) => {
    const listener = (_e: unknown, maximized: boolean): void => cb(maximized)
    ipcRenderer.on('win:maximizedChanged', listener)
    return () => ipcRenderer.removeListener('win:maximizedChanged', listener)
  }
}

export type BrowserApi = typeof browserApi
export type WindowApi = typeof windowApi

if (process.contextIsolated) {
  try {
    contextBridge.exposeInMainWorld('electron', electronAPI)
    contextBridge.exposeInMainWorld('browser', browserApi)
    contextBridge.exposeInMainWorld('win', windowApi)
  } catch (error) {
    console.error(error)
  }
} else {
  // Nhanh nay chi chay khi contextIsolation bi tat. Phai ep kieu vi
  // src/preload/index.d.ts khong nam trong chuong trinh tsconfig.node:
  // TypeScript coi no la file khai bao cua chinh index.ts nen bo qua.
  const globals = window as unknown as Record<string, unknown>
  globals.electron = electronAPI
  globals.browser = browserApi
  globals.win = windowApi
}
