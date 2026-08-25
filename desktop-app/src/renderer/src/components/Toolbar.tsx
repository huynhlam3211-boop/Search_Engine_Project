import { useState, type JSX } from 'react'
import NavigationButtons from './NavigationButtons'
import AddressBar from './AddressBar'
import BrowserMenu from './BrowserMenu'
import Popover from './Popover'
import AccountMenu from './AccountMenu'
import { useSidePanelStore } from '../store/sidePanelStore'
import { useTabStore } from '../store/tabStore'
import { useSessionStore } from '../store/sessionStore'
import { useAdminStore } from '../store/adminStore'
import { DownloadIcon, MenuIcon, PuzzleIcon, SparkleIcon, SplitScreenIcon, UserIcon } from './icons'

function Toolbar(): JSX.Element {
  const [extensionsOpen, setExtensionsOpen] = useState(false)
  const [splitOpen, setSplitOpen] = useState(false)
  const [accountOpen, setAccountOpen] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)

  const openPanel = useSidePanelStore((state) => state.openPanel)
  const togglePanel = useSidePanelStore((state) => state.togglePanel)
  const panelOpen = useSidePanelStore((state) => state.open)
  const tabCount = useTabStore((state) => state.tabs.length)
  const user = useSessionStore((state) => state.user)
  const openDashboard = useAdminStore((state) => state.openDashboard)

  return (
    <div className="flex h-12 shrink-0 items-center gap-1 bg-surface px-2.5">
      <NavigationButtons />
      <div className="mx-1 h-5 w-px shrink-0 bg-line" />

      <AddressBar />

      <div className="ml-0.5 flex shrink-0 items-center gap-0.5">
        <div className="relative">
          <button
            onClick={() => setExtensionsOpen((open) => !open)}
            className={'icon-btn ' + (extensionsOpen ? 'bg-raised text-ink' : '')}
            aria-label="Extensions"
            title="Extensions"
            aria-expanded={extensionsOpen}
          >
            <PuzzleIcon className="h-[18px] w-[18px]" />
          </button>
          <Popover
            open={extensionsOpen}
            onClose={() => setExtensionsOpen(false)}
            label="Extensions"
            width={270}
          >
            <PopoverNote
              title="No extensions installed"
              body="This browser runs a plain WebContentsView and does not load Chrome extensions."
            />
          </Popover>
        </div>

        <button
          onClick={() => togglePanel('ai')}
          className={
            'flex h-8 shrink-0 items-center gap-1.5 rounded-full px-2.5 text-[12.5px] ' +
            'transition-colors duration-150 focus-visible:ring-2 focus-visible:ring-brand/60 ' +
            'focus-visible:outline-none ' +
            (panelOpen === 'ai'
              ? 'bg-brand-soft text-brand'
              : 'text-muted hover:bg-raised hover:text-ink')
          }
          aria-label="Ask AI"
          title="Ask AI"
          aria-pressed={panelOpen === 'ai'}
        >
          <SparkleIcon className="h-4 w-4" />
          Ask AI
        </button>

        <div className="relative">
          <button
            onClick={() => setSplitOpen((open) => !open)}
            className={'icon-btn ' + (splitOpen ? 'bg-raised text-ink' : '')}
            aria-label="Split screen"
            title="Split screen"
            aria-expanded={splitOpen}
          >
            <SplitScreenIcon className="h-[18px] w-[18px]" />
          </button>
          <Popover
            open={splitOpen}
            onClose={() => setSplitOpen(false)}
            label="Split screen"
            width={270}
          >
            <PopoverNote
              title="Split screen is not supported yet"
              body={`This window has ${tabCount} tabs, but only one can be shown at a time.`}
            />
          </Popover>
        </div>

        <button
          onClick={() => openPanel('downloads')}
          className={'icon-btn ' + (panelOpen === 'downloads' ? 'bg-raised text-ink' : '')}
          aria-label="Downloads"
          title="Downloads (Ctrl+J)"
        >
          <DownloadIcon className="h-[18px] w-[18px]" />
        </button>

        <div className="relative">
          <button
            onClick={() => setAccountOpen((open) => !open)}
            className={
              'flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-[11px] ' +
              'font-bold transition hover:brightness-110 focus-visible:ring-2 ' +
              'focus-visible:ring-brand/60 focus-visible:outline-none ' +
              (user
                ? user.role === 'ADMIN'
                  ? 'bg-linear-to-br from-indigo-500 to-violet-500 text-white'
                  : 'bg-linear-to-br from-sky-500 to-teal-400 text-white'
                : 'border border-line bg-raised text-muted')
            }
            aria-label={user ? `Account ${user.username}` : 'Not signed in'}
            title={
              user
                ? `${user.username} — ${user.role === 'ADMIN' ? 'Administrator' : 'User'}`
                : 'Not signed in'
            }
            aria-expanded={accountOpen}
          >
            {user ? user.username.slice(0, 2).toUpperCase() : <UserIcon className="h-4 w-4" />}
          </button>
          <Popover
            open={accountOpen}
            onClose={() => setAccountOpen(false)}
            label="Account"
            width={280}
          >
            <AccountMenu
              onNavigateAdmin={() => {
                setAccountOpen(false)
                openDashboard()
              }}
            />
          </Popover>
        </div>

        <div className="relative">
          <button
            onClick={() => setMenuOpen((open) => !open)}
            className={'icon-btn ' + (menuOpen ? 'bg-raised text-ink' : '')}
            aria-label="Options"
            title="Options"
            aria-expanded={menuOpen}
          >
            <MenuIcon className="h-[18px] w-[18px]" />
          </button>
          <BrowserMenu open={menuOpen} onClose={() => setMenuOpen(false)} />
        </div>
      </div>
    </div>
  )
}

function PopoverNote({ title, body }: { title: string; body: string }): JSX.Element {
  return (
    <div className="px-2.5 py-2">
      <p className="text-[13px] font-medium text-ink">{title}</p>
      <p className="mt-1 text-[12px] leading-relaxed text-faint">{body}</p>
    </div>
  )
}

export default Toolbar
