import { BrowserWindow, ipcMain } from 'electron'

/**
 * Cua so chay o che do frame: false nen Windows khong ve nut thu nho / phong to
 * / dong nua — phai tu lam bang IPC.
 */
export function registerWindowControls(window: BrowserWindow): void {
  ipcMain.on('win:minimize', () => window.minimize())

  ipcMain.on('win:toggleMaximize', () => {
    if (window.isMaximized()) window.unmaximize()
    else window.maximize()
  })

  ipcMain.on('win:close', () => window.close())

  ipcMain.handle('win:isMaximized', () => window.isMaximized())

  const notify = (): void => {
    if (window.isDestroyed()) return
    window.webContents.send('win:maximizedChanged', window.isMaximized())
    for (const view of window.contentView.children) {
      // chromeView khong phai la window.webContents nen phai ban rieng cho no.
      if ('webContents' in view) {
        const wc = (view as { webContents: Electron.WebContents }).webContents
        if (!wc.isDestroyed()) wc.send('win:maximizedChanged', window.isMaximized())
      }
    }
  }

  window.on('maximize', notify)
  window.on('unmaximize', notify)
}
