import { useEffect, useState, type JSX } from 'react'
import { useTabStore, HOME_URL } from '../store/tabStore'
import {
  CloseIcon,
  PlusIcon,
  SpinnerIcon,
  VnSearchMark,
  WinCloseIcon,
  WinMaximizeIcon,
  WinMinimizeIcon,
  WinRestoreIcon
} from './icons'
import { siteGradient, siteInitial } from '../lib/site' 

function TabBar(): JSX.Element {
  const tabs = useTabStore((s) => s.tabs)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const switchTab = useTabStore((s) => s.switchTab)
  const closeTab = useTabStore((s) => s.closeTab)
  const newTab = useTabStore((s) => s.newTab)
  const drag = useWindowDrag()

  return (
    <div className="flex h-10 shrink-0 items-stretch bg-chrome">
      <div className="flex min-w-0 flex-1 items-end gap-px overflow-hidden pl-2 pt-1.5">
          {tabs.map((tab) => (
            <Tab
              key={tab.id}
              tab={tab}
              active={tab.id === activeTabId}
              single={tabs.length === 1}
              onSelect={() => switchTab(tab.id)}
              onClose={() => closeTab(tab.id)}
            />
          ))}

          <button
            onClick={() => newTab()}
            className="mb-1 ml-1 flex h-7 w-7 shrink-0 items-center justify-center rounded-lg
                       text-muted transition-colors hover:bg-surface/70 hover:text-ink 
                       focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/60"
            aria-label="Tab mới"
            title="Tab mới (Trang chủ VnSearch)"
          >
            <PlusIcon className="h-4 w-4"/>
          </button>

          <div className="-mt-1.5 h-10 min-w-[24px] flex-1" {...drag}/>
      </div>

      <WindowControls/>
    </div>
  )
}

interface TabProps { 
  tab: { id: string; url: string; title: string; loading: boolean}
  active: boolean
  single: boolean 
  onSelect: () => void
  onClose: () => void 

}

function Tab({ tab, active, single , onSelect, onClose}: TabProps): JSX.Element {
  const isHome = tab.url === HOME_URL
  const label = tab.loading ? 'Đang tải…' : isHome ? 'Trang chủ VnSearch' : tab.title || tab.url

  return (
    <div
      onClick={onSelect}
      onAuxClick={(e) => {
        if (e.button === 1) {
          e.preventDefault()
          onClose()
        }
      }}
      title={isHome ? 'Trang chủ ' : `${tab.title}\n${tab.url}`}
      className={

      }
    >
      <span>

      </span>

      <span>

      </span>

      <button
        className={

        }
        aria-label={}
        title="Đóng tab" 
      >
         <CloseIcon/>
      </button>

    </div>
  )
}

function WindowControls(): JSX.Element {
  const[maximized, setMaximized] = useState(false)

  useEffect(() => {
    window.win.isMaximized().then(setMaximized)
    window.win.onMaximizedChanged(setMaximized)
  }, [])

  const base = 
      'flex h-10 w-[46px] shrink-0 items-center justify-center text-muted transition-colors' + 
      'hover:bg-black/[0.06] hover:text-ink focus-visible:outline-none dark:hover:bg-white/10'
  return (
    <div className="flex shrink-0 items-start">
      <button>
        <WinMinimizeIcon className="h-[10px] w-[10px]" />
      </button>
      <button>
        {maximized ? ( 
          <WinRestoreIcon className ="h-[10px] w-[10px]"/>
        ) : (
          <WinMaximizeIcon class
        )}
      </button>
      <button
        onClick={() => window.win.close()}
        className={base + ' hover:!bg-[#c42b1c] hover:!text-white'} 
        aria-label="Đóng cửa sổ"
        title="Đóng"
      >
        <WindowCloseIcon className="h-[10px] w-[10px]"/>
      </button>

    </div>
  )

}

functon useWindowDrag(): { onMouseDown: (e: React.MouseEvent) => void; onDoubleClick: () => void } {
  const dragging = useRef(false)

  useEffect(() => {
    const stop = (): void => {
      if (dragging.current) {
        dragging.current = false
        window.win.dragEnd()
      }
    }
    window.addEventListener('mouseup', stop)
    window.addEventListener('blur', stop)
    return () => {
      window.removeEventListener('mouseup', stop)
      window.removeEventListener('blur', stop)
    }
  }, [])

  function onMouseDown(e: React.MouseEvent): void {
    if (e.button !== 0) {
      return 
    }

    const startX = e.screenX
    const startY = e.screenY 

    const onMove = (move: MouseEvent) : void => {
      if (Math.abs(move.screenX - startX) + Math.abs(move.screenY - startY) < 4) {
        return 
      }
      window.removeEventListenerEventListener('mousemove', onMove)
      dragging.current = true
      window.win.dragStart()
    }
  

    const onUp =(): void => {
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseup', onUp) 
    }

    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseup', onUp)

  }

  return { onMouseDown, onDoubleClick: () => window.win.toggleMaximize() }
}


export default TabBar
