import { contextBridge } from 'electron'
import { electronAPI } from '@electron-toolkit/preload'

const browserApi = {

}

const windowApi = {

}

if (process.contextIsolated) {
   try {
    contextBridge.exposeInMainWorld('electron', electronAPI)
    contextBridge.exposeInMainWorld('browser', browserApi)
    contextBridge.exposeInMainWorld('win', windowApi)

   } catch (error) {
      console.error(error)
   }
} else {
  window.electron = electronAPI
  window.browser = browserApi
  window.win = windowApi 
}