import { create } from './createStore'
import { useHistoryStore } from './historyStore'

/** Phai trung voi HOME_URL trong src/main/tabManager.ts. */
export const HOME_URL = 'vnsearch://home'

export interface TabState {
  id: string
  url: string
  title: string
  loading: boolean
  canGoBack: boolean
  canGoForward: boolean
}

interface TabStore {
  tabs: TabState[]
  activeTabId: string | null
  init: () => void
  createTab: (url?: string) => void
  closeTab: (id: string) => void
  switchTab: (id: string) => void
  /** Mo URL trong tab dang hoat dong. */
  navigate: (url: string) => void
  goBack: () => void
  goForward: () => void
  reload: () => void
  goHome: () => void
}

export const useTabStore = create<TabStore>((set, get) => ({
  tabs: [],
  activeTabId: null,

  init: () => {
    // Main process la nguon su that duy nhat ve tab: renderer chi nghe va ve lai.
    window.browser.onTabsChanged(({ tabs, activeTabId }) => {
      const active = tabs.find((t) => t.id === activeTabId)
      // Ghi lich su khi trang da tai xong, luc do title moi dung.
      if (active && active.url !== HOME_URL && !active.loading) {
        useHistoryStore.getState().add(active.url, active.title)
      }
      set({ tabs, activeTabId })
    })
    window.browser.listTabs().then(({ tabs, activeTabId }) => set({ tabs, activeTabId }))
  },

  createTab: (url) => {
    window.browser.createTab(url)
  },
  closeTab: (id) => {
    window.browser.closeTab(id)
  },
  switchTab: (id) => {
    window.browser.switchTab(id)
  },

  navigate: (url) => {
    const id = get().activeTabId
    if (id) window.browser.navigate(id, url)
  },
  goBack: () => {
    const id = get().activeTabId
    if (id) window.browser.goBack(id)
  },
  goForward: () => {
    const id = get().activeTabId
    if (id) window.browser.goForward(id)
  },
  reload: () => {
    const id = get().activeTabId
    if (id) window.browser.reload(id)
  },
  goHome: () => {
    const id = get().activeTabId
    if (id) window.browser.navigate(id, HOME_URL)
  }
}))

export function getActiveTab(): TabState | undefined {
  const { tabs, activeTabId } = useTabStore.getState()
  return tabs.find((t) => t.id === activeTabId)
}
