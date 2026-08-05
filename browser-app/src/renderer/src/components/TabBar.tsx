import { useEffect, useState, type JSX } from 'react'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { CloseIcon, PlusIcon, VnSearchMark, WinClose, WinMaximize, WinMinimize } from './icon'

function tabLabel(url: string, title: string): string {
  if (url === HOME_URL) return 'Tab mới'
  return title || url
}

function TabBar(): JSX.Element {
  const tabs = useTabStore((s) => s.tabs)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const switchTab = useTabStore((s) => s.switchTab)
  const closeTab = useTabStore((s) => s.closeTab)
  const createTab = useTabStore((s) => s.createTab)
  const [maximized, setMaximized] = useState(false)

  useEffect(() => {
    window.win.isMaximized().then(setMaximized)
    return window.win.onMaximizedChanged(setMaximized)
  }, [])

  return (
    <div className="tabbar drag">
      
    </div>
  )
}

export default TabBar
