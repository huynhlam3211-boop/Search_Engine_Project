import { useState, type JSX } from 'react'
import NavigationButtons from './NavigationButtons'
import AddressBar from './AddressBar'
import BrowserMenu from './BrowserMenu'
import Popover from './Popover'
import { useSidePanelStore } from '../store/sidePanelStore'
import { useTabStore } from '../store/tabStore'
import { ACCOUNT } from '../lib/account'
import {
  DowloadIcon,
  MenuIcon,
  PuzzleIcon,
  SparkleIcon,
  SplitScreenIcon

} from './icons'


function Toolbar(): JSX.Element {
  const [extensionsOpen, setExtensionsOpen] = useState(false)
  const [splitOpen, setSplitOpen] = useState(false)
  const [accountOpen, setAccountOpen] = useState(false)
  const [menuOpen, setMenuOpen] = useState(false)

  const openPanel = useSidePanelStore((s) => s.openPanel)
  const togglePanel = useSidePanelStore((s) => s.togglePanel)
  const panelOpen = useSidePanelStore((s) => s.open)
  const tabCount = useTabStore((s) => s.tabs.length)

  return (
    <div className="flex h-12 shrink-0 items-center gap-1 bg-surface px-2.5">
      <NavigationButtons />
      <div className="mx-1 h-5 w-px shrink-0 bg-line">
      <AddressBar />

      <div className="ml-0.5 flex shrink-0 items-center gap-0.5">
        <div className="relative">
          <button>
            <PuzzleIcon className="h-[18px] w-[18px]"/>
          </button>
          <Popover
              open={extensionsOpen}
              onClose={() => setExtensionsOpen(false)}
              label="Tiện ích mở rộng"
              width={270}
          >
            <PopoverNote
              title="Chưa cài tiện ích nào"
              body="Trình duyệt này chạy WebContentsView thuần , chưa nạp tiện ích Chrome"
            />
          </Popover>
        </div>

        <button>
          <SparkleIcon className="h-4 w-4"/>
          Hỏi AI
        </button>

      </div>

      </div>
      <AddressBar />

      <div className="toolbar__end">
        <button type="button" title="Tuỳ chọn" onClick={() => setMenuOpen((v) => !v)}>
          <MenuIcon />
        </button>
        <BrowserMenu open={menuOpen} onClose={() => setMenuOpen(false)} />
      </div>
    </div>
  )
}

function PopoverNote({title, body}: { title: string; body: string}) : JSX.Element {
  return (
    <div className="px-2.5 py-2">
      <p className="text-[13px] font-medium text-ink">{title}</p>
      <p className="mt-1 text-[12px] leading-relaxed text-faint">{body}</p>
    </div>
  )
}

export default Toolbar
