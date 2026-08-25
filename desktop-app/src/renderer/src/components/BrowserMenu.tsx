import { useEffect, useRef, useState, type JSX } from 'react'
import { useShallow } from 'zustand/shallow'
import Popover from './Popover'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { useHistoryStore } from '../store/historyStore'
import { useSidePanelStore } from '../store/sidePanelStore'
import { useZoomStore, zoomFactorOf } from '../store/zoomStore'
import { useSessionStore } from '../store/sessionStore'
import { hostOf, siteGradient, siteInitial } from '../lib/site'
import {
  ChevronRightIcon,
  ClockIcon,
  DeviceIcon,
  DownloadIcon,
  ExitIcon,
  FullscreenIcon,
  HelpIcon,
  IncognitoIcon,
  MinusIcon,
  PlusIcon,
  PrintIcon,
  PuzzleIcon,
  SearchIcon,
  SettingsIcon,
  StarIcon,
  TranslateIcon,
  TrashIcon,
  VnSearchMark,
  WindowIcon
} from './icons'

interface BrowserMenuProps {
  open: boolean
  onClose: () => void
}

function BrowserMenu({ open, onClose }: BrowserMenuProps): JSX.Element {
  const newTab = useTabStore((s) => s.newTab)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const tabs = useTabStore((s) => s.tabs)
  const openPanel = useSidePanelStore((s) => s.openPanel)
  const clearAll = useHistoryStore((s) => s.clearAll)

  const activeTab = tabs.find((t) => t.id === activeTabId)
  const onExternalPage = !!activeTab && activeTab.url !== HOME_URL

  function run(action: () => void): void {
    action()
    onClose()
  }

  return (
    <Popover open={open} onClose={onClose} width={304} label="Browser menu">
      <AccountRow />

      <div className="menu-sep" />

      <MenuItem
        icon={<VnSearchMark className="h-4 w-4" />}
        label="New tab"
        shortcut="Ctrl+T"
        onClick={() => run(() => newTab())}
      />
      <MenuItem
        icon={<WindowIcon className="h-[17px] w-[17px]" />}
        label="New window"
        shortcut="Ctrl+N"
        disabled
        title="The app currently runs in a single window only."
      />
      <MenuItem
        icon={<IncognitoIcon className="h-[17px] w-[17px]" />}
        label="New incognito window"
        shortcut="Ctrl+Shift+N"
        disabled
        title="There is no separate private browsing session yet."
      />

      <div className="menu-sep" />

      <HistoryItem onClose={onClose} />
      <MenuItem
        icon={<DownloadIcon className="h-[17px] w-[17px]" />}
        label="Downloads"
        shortcut="Ctrl+J"
        onClick={() => run(() => openPanel('downloads'))}
      />
      <MenuItem
        icon={<StarIcon className="h-[17px] w-[17px]" />}
        label="Bookmarks"
        onClick={() => run(() => openPanel('bookmarks'))}
      />

      <div className="menu-sep" />

      <MenuItem
        icon={<PuzzleIcon className="h-[17px] w-[17px]" />}
        label="Extensions"
        disabled
        title="No Chrome extensions are loaded."
      />
      <MenuItem
        icon={<TrashIcon className="h-[17px] w-[17px]" />}
        label="Clear browsing data"
        shortcut="Ctrl+Shift+Del"
        onClick={() => run(clearAll)}
        title="Clears the back/forward stacks of every tab."
      />

      <div className="menu-sep" />

      <ZoomRow onClose={onClose} enabled={onExternalPage} />

      <div className="menu-sep" />

      <MenuItem
        icon={<PrintIcon className="h-[17px] w-[17px]" />}
        label="Print…"
        shortcut="Ctrl+P"
        disabled={!onExternalPage}
        title={onExternalPage ? undefined : 'Only a web page open in a tab can be printed.'}
        onClick={() => run(() => activeTabId && window.browser.print(activeTabId))}
      />
      <MenuItem
        icon={<TranslateIcon className="h-[17px] w-[17px]" />}
        label="Translate…"
        disabled
        title="No translation service is connected yet."
      />
      <MenuItem
        icon={<SearchIcon className="h-[17px] w-[17px]" />}
        label="Find in page…"
        shortcut="Ctrl+F"
        disabled
        title="Find in page is not implemented yet."
      />

      <div className="menu-sep" />

      <MenuItem
        icon={<HelpIcon className="h-[17px] w-[17px]" />}
        label="Help"
        disabled
        title="See the project's README.md."
      />
      <MenuItem
        icon={<SettingsIcon className="h-[17px] w-[17px]" />}
        label="Settings"
        disabled
        title="There is no settings page yet."
      />
      <MenuItem
        icon={<ExitIcon className="h-[17px] w-[17px]" />}
        label="Exit"
        onClick={() => window.win.close()}
      />
    </Popover>
  )
}

interface MenuItemProps {
  icon: JSX.Element
  label: string
  shortcut?: string
  onClick?: () => void
  disabled?: boolean
  title?: string
}

function MenuItem({ icon, label, shortcut, onClick, disabled, title }: MenuItemProps): JSX.Element {
  return (
    <button onClick={onClick} disabled={disabled} className="menu-row" title={title}>
      <span className="shrink-0 text-muted">{icon}</span>
      <span className="min-w-0 flex-1 truncate">{label}</span>
      {shortcut && <span className="shrink-0 text-[12px] text-faint">{shortcut}</span>}
    </button>
  )
}

function AccountRow(): JSX.Element {
  const user = useSessionStore((state) => state.user)
  const isAdmin = user?.role === 'ADMIN'

  return (
    <button
      className="menu-row py-2"
      title={user ? 'Manage your account from the avatar button on the toolbar' : 'Not signed in'}
    >
      <span
        className={
          'flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[12px] font-bold ' +
          (user
            ? isAdmin
              ? 'bg-gradient-to-br from-indigo-500 to-violet-500 text-white'
              : 'bg-gradient-to-br from-sky-500 to-teal-400 text-white'
            : 'border border-line bg-raised text-muted')
        }
      >
        {user ? user.username.slice(0, 2).toUpperCase() : '—'}
      </span>
      <span className="min-w-0 flex-1">
        <span className="block truncate text-[13px] font-medium text-ink">
          {user ? user.username : 'Not signed in'}
        </span>
        <span className={'block truncate text-[12px] ' + (isAdmin ? 'text-success' : 'text-muted')}>
          {user ? (isAdmin ? 'Administrator' : 'User') : 'Click the avatar to sign in'}
        </span>
      </span>
      <ChevronRightIcon className="h-4 w-4 shrink-0 text-faint" />
    </button>
  )
}

function ZoomRow({ onClose, enabled }: { onClose: () => void; enabled: boolean }): JSX.Element {
  const activeTabId = useTabStore((state) => state.activeTabId)
  const factor = useZoomStore((state) => zoomFactorOf(state.factors, activeTabId))
  const zoomIn = useZoomStore((state) => state.zoomIn)
  const zoomOut = useZoomStore((state) => state.zoomOut)
  const reset = useZoomStore((state) => state.reset)

  const button =
    'flex h-7 w-7 items-center justify-center rounded-full text-muted transition-colors ' +
    'hover:bg-line hover:text-ink focus-visible:outline-none focus-visible:ring-2 ' +
    'focus-visible:ring-brand/60 disabled:pointer-events-none disabled:text-faint/50'

  return (
    <div
      className="flex items-center gap-3 px-2.5 py-1.5 text-[13px] text-ink"
      title={enabled ? undefined : 'Only a web page open in a tab can be zoomed.'}
    >
      <span className="flex-1">Zoom</span>
      <button onClick={zoomOut} disabled={!enabled} className={button} aria-label="Zoom out">
        <MinusIcon className="h-4 w-4" />
      </button>
      <button
        onClick={reset}
        disabled={!enabled}
        className="w-12 shrink-0 rounded-md py-0.5 text-center text-[13px] tabular-nums text-ink
                   transition-colors hover:bg-line focus-visible:outline-none
                   disabled:pointer-events-none disabled:text-faint/60"
        aria-label="Reset zoom to 100%"
        title="Reset to 100%"
      >
        {Math.round(factor * 100)}%
      </button>
      <button onClick={zoomIn} disabled={!enabled} className={button} aria-label="Zoom in">
        <PlusIcon className="h-4 w-4" />
      </button>
      <button
        onClick={() => {
          window.win.toggleFullScreen()
          onClose()
        }}
        className={button}
        aria-label="Full screen"
        title="Full screen (F11)"
      >
        <FullscreenIcon className="h-4 w-4" />
      </button>
    </div>
  )
}

function HistoryItem({ onClose }: { onClose: () => void }): JSX.Element {
  const [open, setOpen] = useState(false)
  const timer = useRef<number | undefined>(undefined)

  const navigate = useTabStore((state) => state.navigate)
  const recent = useHistoryStore(
    useShallow((state) => state.recentUrls(8).filter((url) => url !== HOME_URL))
  )

  useEffect(() => () => window.clearTimeout(timer.current), [])

  function show(): void {
    window.clearTimeout(timer.current)
    setOpen(true)
  }

  function hideSoon(): void {
    window.clearTimeout(timer.current)
    timer.current = window.setTimeout(() => setOpen(false), 180)
  }

  return (
    <div className="relative" onMouseEnter={show} onMouseLeave={hideSoon}>
      <button className="menu-row" aria-haspopup="true" aria-expanded={open}>
        <span className="shrink-0 text-muted">
          <ClockIcon className="h-[17px] w-[17px]" />
        </span>
        <span className="min-w-0 flex-1 truncate">History</span>
        <ChevronRightIcon className="h-4 w-4 shrink-0 text-faint" />
      </button>

      {open && (
        <div
          className="absolute right-[calc(100%+8px)] top-0 z-50 w-[290px] animate-scale-in rounded-xl
                     border border-line bg-surface p-1.5 shadow-pop"
          role="menu"
          aria-label="History"
        >
          <p className="px-2.5 py-1.5 text-[12px] font-semibold uppercase tracking-wide text-faint">
            Recent tabs
          </p>

          {recent.length === 0 ? (
            <p className="px-2.5 pb-2 text-[12px] text-faint">
              You haven&apos;t visited any page in this session.
            </p>
          ) : (
            recent.map((url) => (
              <button
                key={url}
                onClick={() => {
                  navigate(url)
                  onClose()
                }}
                className="menu-row"
                title={url}
              >
                <span
                  className="flex h-4 w-4 shrink-0 items-center justify-center rounded-[5px]
                             text-[9px] font-bold text-white"
                  style={{ background: siteGradient(url) }}
                >
                  {siteInitial(url)}
                </span>
                <span className="min-w-0 flex-1 truncate">{hostOf(url)}</span>
              </button>
            ))
          )}

          <div className="menu-sep" />

          <p className="px-2.5 py-1.5 text-[12px] font-semibold uppercase tracking-wide text-faint">
            Your devices
          </p>
          <div className="flex items-start gap-3 px-2.5 pb-2 text-[12px] text-faint">
            <DeviceIcon className="mt-0.5 h-4 w-4 shrink-0" />
            <span className="leading-relaxed">
              No devices are synced yet — the app has no account server.
            </span>
          </div>
        </div>
      )}
    </div>
  )
}

export default BrowserMenu
