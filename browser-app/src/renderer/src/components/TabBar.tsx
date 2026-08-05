import { useEffect, useRef, useState } from 'react'
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
          title="Tab mới (trang chủ VnSearch)"
        >
          <PlusIcon className="h-4 w-4" />
        </button>

        <div className="-mt-1.5 h-10 min-w-[24px] flex-1" {...drag} />
      </div>

      <WindowControls />
    </div>
  )
}

interface TabProps {
  tab: { id: string; url: string; title: string; loading: boolean }
  active: boolean
  single: boolean
  onSelect: () => void
  onClose: () => void
}

function Tab({ tab, active, single, onSelect, onClose }: TabProps): JSX.Element {
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
      title={isHome ? 'Trang chủ VnSearch' : `${tab.title}\n${tab.url}`}
      className={
        'group relative flex h-[34px] min-w-0 max-w-[240px] flex-1 cursor-default items-center ' +
        'gap-2 rounded-t-[10px] pl-2.5 pr-1.5 text-[13px] transition-colors duration-150 ' +
        (active
          ? 'z-10 bg-surface text-ink shadow-tab'
          : 'text-muted hover:bg-surface/45 hover:text-ink')
      }
    >

      {!active && !single && (
        <span className="absolute right-0 top-1/2 h-4 w-px -translate-y-1/2 bg-line group-hover:opacity-0" />
      )}

      <span className="flex h-4 w-4 shrink-0 items-center justify-center">
        {tab.loading ? (
          <SpinnerIcon className="h-3.5 w-3.5 text-brand" />
        ) : isHome ? (
          <VnSearchMark className="h-4 w-4 text-muted" />
        ) : (
          <span
            className="flex h-4 w-4 items-center justify-center rounded-[5px] text-[9px] font-bold text-white"
            style={{ background: siteGradient(tab.url) }}
          >
            {siteInitial(tab.url)}
          </span>
        )}
      </span>

      <span className="relative min-w-0 flex-1 overflow-hidden whitespace-nowrap">
        {label}
        <span
          className={
            'pointer-events-none absolute inset-y-0 right-0 w-8 bg-gradient-to-l to-transparent ' +
            (active ? 'from-surface' : 'from-chrome')
          }
        />
      </span>

      <button
        onClick={(e) => {
          e.stopPropagation()
          onClose()
        }}
        className={
          'flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-muted ' +
          'transition hover:bg-danger/15 hover:text-danger focus-visible:outline-none ' +
          'focus-visible:ring-2 focus-visible:ring-brand/60 ' +
          (active ? 'opacity-100' : 'opacity-0 group-hover:opacity-100')
        }
        aria-label={`Đóng tab ${label}`}
        title="Đóng tab"
      >
        <CloseIcon className="h-3 w-3" strokeWidth={2.2} />
      </button>
    </div>
  )
}

function WindowControls(): JSX.Element {
  const [maximized, setMaximized] = useState(false)

  useEffect(() => {
    window.win.isMaximized().then(setMaximized)
    window.win.onMaximizeChanged(setMaximized)
  }, [])

  const base =
    'flex h-10 w-[46px] shrink-0 items-center justify-center text-muted transition-colors ' +
    'hover:bg-black/[0.06] hover:text-ink focus-visible:outline-none dark:hover:bg-white/10'

  return (
    <div className="flex shrink-0 items-start">
      <button onClick={() => window.win.minimize()} className={base} aria-label="Thu nhỏ" title="Thu nhỏ">
        <WinMinimizeIcon className="h-[10px] w-[10px]" />
      </button>
      <button
        onClick={() => window.win.toggleMaximize().then(setMaximized)}
        className={base}
        aria-label={maximized ? 'Khôi phục cửa sổ' : 'Phóng to'}
        title={maximized ? 'Khôi phục cửa sổ' : 'Phóng to'}
      >
        {maximized ? (
          <WinRestoreIcon className="h-[10px] w-[10px]" />
        ) : (
          <WinMaximizeIcon className="h-[10px] w-[10px]" />
        )}
      </button>
      <button
        onClick={() => window.win.close()}
        className={base + ' hover:!bg-[#c42b1c] hover:!text-white'}
        aria-label="Đóng cửa sổ"
        title="Đóng"
      >
        <WinCloseIcon className="h-[10px] w-[10px]" />
      </button>
    </div>
  )
}
 

function useWindowDrag(): { onMouseDown: (e: React.MouseEvent) => void; onDoubleClick: () => void } {
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

    const onMove = (move: MouseEvent): void => {
      if (Math.abs(move.screenX - startX) + Math.abs(move.screenY - startY) < 4) {
        return
      }
      window.removeEventListener('mousemove', onMove)
      dragging.current = true
      window.win.dragStart()
    }

    const onUp = (): void => {
      window.removeEventListener('mousemove', onMove)
      window.removeEventListener('mouseup', onUp)
    }

    window.addEventListener('mousemove', onMove)
    window.addEventListener('mouseup', onUp)
  }

  return { onMouseDown, onDoubleClick: () => window.win.toggleMaximize() }
}

export default TabBar
