import { ElectronAPI } from '@electron-toolkit/preload'

interface BrowserApi {

}

interface WindowApi {
  
}

declare global {
  interface Window {
    electron: ElectronAPI
    api: unknown
  }
}
