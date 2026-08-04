import { ipcMain } from 'electron'
import { TabManager } from './tabManager'

export function registerIpcHandler(tabManager: TabManager): void {
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    ipcMain.handle('browser:listTabs', () => tabManager.listTabs())
    
}