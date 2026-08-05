import { ipcMain } from 'electron'
import { TabManager } from './tabManager'

export function registerIpcHandlers(tabManager: TabManager): void {
  ipcMain.handle('browser:listTabs', () => ({
    tabs: tabManager.listTabs(),
    activeTabId: tabManager.getActiveTabId()
  }))

  ipcMain.handle('browser:createTab', (_e, url?: string) => tabManager.createTab(url))
  ipcMain.handle('browser:closeTab', (_e, id: string) => tabManager.closeTab(id))
  ipcMain.handle('browser:switchTab', (_e, id: string) => tabManager.switchTab(id))
  ipcMain.handle('browser:navigate', (_e, id: string, url: string) => tabManager.navigate(id, url))
  ipcMain.handle('browser:goBack', (_e, id: string) => tabManager.goBack(id))
  ipcMain.handle('browser:goForward', (_e, id: string) => tabManager.goForward(id))
  ipcMain.handle('browser:reload', (_e, id: string) => tabManager.reload(id))
  ipcMain.handle('browser:print', (_e, id: string) => tabManager.print(id))
  ipcMain.handle('browser:setZoom', (_e, id: string, factor: number) =>
    tabManager.setZoom(id, factor)
  )

  // Hai kenh nay ban lien tuc theo layout nen dung `on` cho nhe: khong can tra ve gi.
  ipcMain.on('browser:setPanelWidth', (_e, px: number) => tabManager.setPanelWidth(px))
  ipcMain.on('browser:setOverlay', (_e, active: boolean) => tabManager.setOverlay(active))
}
