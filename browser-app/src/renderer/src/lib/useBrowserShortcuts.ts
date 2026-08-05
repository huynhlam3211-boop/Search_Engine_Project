import { useEffect } from 'react'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { useBookmarkStore } from '../store/bookmarkStore'

/** O dia chi mang id nay de phim tat Ctrl+L / Alt+D goi den duoc. */
export const OMNIBOX_ID = 'vnsearch-omnibox'

function focusOmnibox(): void {
  const input = document.getElementById(OMNIBOX_ID) as HTMLInputElement | null
  input?.focus()
  input?.select()
}

function runShortcut(name: string): void {
  const tabs = useTabStore.getState()
  const active = tabs.tabs.find((t) => t.id === tabs.activeTabId)

  switch (name) {
    case 'newTab':
      tabs.createTab()
      focusOmnibox()
      break
    case 'closeTab':
      if (tabs.activeTabId) tabs.closeTab(tabs.activeTabId)
      break
    case 'focusOmnibox':
      focusOmnibox()
      break
    case 'reload':
      tabs.reload()
      break
    case 'back':
      tabs.goBack()
      break
    case 'forward':
      tabs.goForward()
      break
    case 'home':
      tabs.goHome()
      break
    case 'bookmark':
      if (active && active.url !== HOME_URL) {
        useBookmarkStore.getState().toggleBookmark(active.url, active.title)
      }
      break
  }
}

/**
 * Phim tat den tu hai huong: ban phim go thang vao giao dien React, va su kien
 * do main process chuyen ve khi tieu diem dang nam trong trang web.
 */
export function useBrowserShortcuts(): void {
  useEffect(() => {
    const unsubscribe = window.browser.onShortcut(runShortcut)

    const onKeyDown = (e: KeyboardEvent): void => {
      const key = e.key.toLowerCase()
      let name: string | null = null

      if (e.ctrlKey && !e.altKey && !e.shiftKey) {
        if (key === 't') name = 'newTab'
        else if (key === 'w') name = 'closeTab'
        else if (key === 'l') name = 'focusOmnibox'
        else if (key === 'd') name = 'bookmark'
        else if (key === 'r') name = 'reload'
      } else if (e.altKey && !e.ctrlKey) {
        if (key === 'd') name = 'focusOmnibox'
        else if (key === 'arrowleft') name = 'back'
        else if (key === 'arrowright') name = 'forward'
        else if (key === 'home') name = 'home'
      } else if (key === 'f5') {
        name = 'reload'
      }

      if (!name) return
      e.preventDefault()
      runShortcut(name)
    }

    window.addEventListener('keydown', onKeyDown)
    return () => {
      unsubscribe()
      window.removeEventListener('keydown', onKeyDown)
    }
  }, [])
}
