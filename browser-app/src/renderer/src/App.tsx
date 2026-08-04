import { useEffect } from 'react'
import TabBar from './components/TabBar'
import Toolbar from './components/Toolbar'
import BookmarksBar from './components/BookmarksBar'
import SideRail from './components/SideRail'
import SidePanel from './components/SidePanel'
import NewTabPage from './components/NewTabPage'
import SearchResultList from './components/SearchResultList'
import { useTabStore, HOME_URL } from './store/tabStore'
import { useSearchViewStore } from './store/searchViewStore'
import { useOverlayStore } from './store/overlayStore'
import { useSidePanelStore, PANEL_WIDTH } from './store/sidePanelStore'
import { useBrowserShortcuts } from './lib/useBrowserShortcuts'

function App() {
  const init = useTabStore((s) => s.init)
  const showInternalContent = useTabStore((s) => {
    const tab = s.tabs.find((t) => t.id === s.activeTabId)
    return !tab || tab.url === HOME_URL
  })

  const hasQuery = useSearchViewStore((s) => s.query.length > 0)
  const hasOverlay = useOverlayStore((s) => s.count > 0)
  const panelWidth = useSidePanelStore((s) => (s.open ? PANEL_WIDTH : 0))

  useBrowserShortcuts()

  useEffect(() => {
    init()
  }, [init])

  useEffect(() => {
    window.browser.setPanelWidth(panelWidth)
  }, [panelWidth])

  useEffect(() => {
    window.browser.setOverlay(hasOverlay)
  }, [hasOverlay])

  return (
    <div className="flex h-screen w-screen flex-col overflow-hidden bg-chrome text-ink">
      <TabBar />
      <Toolbar />
      <BookmarksBar />

      <div className="flex min-h-0 flex-1">
        <main className="min-w-0 flex-1 bg-surface">
          {showInternalContent && (hasQuery ? <SearchResultList /> : <NewTabPage />)}
        </main>
        <SidePanel />
        <SideRail />
      </div>
    </div>
  )
}

export default App