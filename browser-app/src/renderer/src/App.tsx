import { useEffect, type JSX } from 'react'
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

function App(): JSX.Element {
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

  // Main process can biet ben phai con bao nhieu cho de dat lai WebContentsView.
  useEffect(() => {
    window.browser.setPanelWidth(panelWidth)
  }, [panelWidth])

  useEffect(() => {
    window.browser.setOverlay(hasOverlay)
  }, [hasOverlay])

  return (
    <div className="app">
      <TabBar />
      <Toolbar />
      <BookmarksBar />

      <div className="app__body">
        <main className="app__content">
          {showInternalContent && (hasQuery ? <SearchResultList /> : <NewTabPage />)}
        </main>
        <SidePanel />
        <SideRail />
      </div>
    </div>
  )
}

export default App
