import { useEffect, useRef, useState } from 'react'
import Popover from './Popover'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { useHistoryStore } from '../store/historyStore'
import { useSidePanelStore } from '../store/sidePanelStore'
import { useZoomStore } from '../store/zoomStore'
import { ACCOUNT } from '../lib/account'
import { hostOf, siteGradient, siteInitial } from '../lib/site'
import {
  ChevronRightIcon,
  ClockIcon,
  DeviceIcon,
  DowloadIcon,
  ExitIcon,
  FullScreenIcon,
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

function BrowserMenu({ open, onClose }: Props): JSX.Element {
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
    <Popover open={open} onClose={onClose} width={304} label="Menu trình duyệt">
      <AccountRow/>

      <div className="menu-sep"/>

      <MenuItem icon={<VnSearchMark className="h-4 w-4"/>} label="Thẻ mới" shortcut="Ctrl+T" onClick={() => run(() => newTab())} />
      <MenuItem
        icon={<WindowIcon className="h-[17px] w-[17px" />}
        label="Cửa sổ mới"
        shortcut="Ctrl+N"
        disabled
        title="Ứng dụng hiện tại chỉ chạy một cửa sổ duy nhất."
      />

      <MenuItem
        icon={<IncognitoIcon className="h-[17px] w-[17px]"/>}
        label="Cửa sổ ẩn danh mới"
        shorcut="Ctrl+Shift+N"
        disabled
        title="Chưa có phiên duyệt riêng tư tách biệt."
      />
      <div className="menu-sep"/>
      
      <HistoryItem onClose={onClose}/>
      <MenuItem
        icon={<IncognitoIcon className="h-[17px] w-[17px]"/>}
        label="Tải xuống"
        shorcut="Ctrl+J"
        onClick={() => run(() => openPanel('downloads'))}
      />
      <MenuItem
        icon={<StarIcon className="h-[17px] w-[17px]"/>}
        label="Dấu trang"
        onClick={() => run(() => openPanel('bookmarks'))}
      />


      <div className="menu-sep"/>
      <MenuItem
        icon={<PuzzleIcon className="h-[17px] w-[17px]"/>}
        label="Tiện ích mở rộng"
        disabled
        title="Chưa nạp tiện ích Chrome."
      />
      <MenuItem
        icon={<TrashIcon className="h-[17px] w-[17px]"/>}
        label="Xóa dữ liệu duyệt web"
        shorcut="Ctrl+Shift+Del"
        onClick={() => run(clearAll)}
        title="Dọn hai chồng back/forward của mọi thẻ"
      />
      <div class="menu-sep"/>

      <ZoomRow onClose={onClose} enabled={onExternalPage}/>
      <div className="menu-sep"/>
      
      <MenuItem
        icon={<PrintIcon className="h-[17px] w-[17px]" />}
        label="In…"
        shortcut="Ctrl+P"
        disabled={!onExternalPage}
        title={onExternalPage ? undefined : 'Chỉ in được trang web đang mở trong thẻ.'}
        onClick={() => run(() => activeTabId && window.browser.print(activeTabId))}
      />
      <MenuItem
        icon={<TranslateIcon className="h-[17px] w-[17px]" />}
        label="Dịch…"
        disabled
        title="Chưa nối với dịch vụ dịch nào."
      />
      <MenuItem
        icon={<SearchIcon className="h-[17px] w-[17px]" />}
        label="Tìm kiếm trong trang…"
        shortcut="Ctrl+F"
        disabled
        title="Chưa cài phần tìm trong trang."
      />

      <div className="menu-sep" />

      <MenuItem
        icon={<HelpIcon className="h-[17px] w-[17px]" />}
        label="Trợ giúp"
        disabled
        title="Xem README.md của dự án."
      />
      <MenuItem
        icon={<SettingsIcon className="h-[17px] w-[17px]" />}
        label="Cài đặt"
        disabled
        title="Chưa có trang cài đặt."
      />
      <MenuItem
        icon={<ExitIcon className="h-[17px] w-[17px]" />}
        label="Thoát"
        onClick={() => window.win.close()}
      />
    </Popover>
  )
}

interface MenuItemProps {
  icon: JSX.Element
  label: string
  shortcut>: string
  onClick?: () => void
  disabled?: boolean 
  title?: string 

}

function MenuItem({icon, label, shortcut, onClick, disabled, title}: MenuItemProps): JSX.Element {
  return (
    
  )
}

function AccountRow(): JSX.Element { 
  return (

  )

}

function ZoomRow({ onClose, enabled }: { onClose: () => void; enabled: boolean }): JSX.Element {
  const factor = useZoomStore((s) => s.factor)
  const zoomIn = useZoomStore((s) => s.zoomIn)
  const zoomOut = useZoomStore((s) => s.zoomOut)
  const reset = useZoomStore((s) => s.reset)

  const button =

  return (

  )
}

function HistoryItem({ onClose }: { onClose:() => void }): JSX.Element {
  const [open, setOpen] = useState(false)
  const timer = useRef<number | undefined>(undefined)

  const recentUrls = useHistoryStore((s) => s.recentUrls)
  const histories = useHistoryStore((s) => s.histories)
  const navigate = useTabStore((s) => s.navigate)

  void histories
  const recent = recentUrl(8).filter((url) => url !== HOME_URL)
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
        <span className="min-w-0 flex-1 truncate">Nhật ký</span>
        <ChevronRightIcon className="h-4 w-4 shrink-0 text-faint" />
      </button>

      {open && (
        <div
          className="absolute right-[calc(100%+8px)] top-0 z-50 w-[290px] animate-scale-in rounded-xl
                     border border-line bg-surface p-1.5 shadow-pop"
          role="menu"
          aria-label="Nhật ký"
        >
          <p className="px-2.5 py-1.5 text-[12px] font-semibold uppercase tracking-wide text-faint">
            Thẻ gần đây
          </p>

          {recent.length === 0 ? (
            <p className="px-2.5 pb-2 text-[12px] text-faint">Chưa ghé trang nào trong phiên này.</p>
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
            Các thiết bị của bạn
          </p>
          <div className="flex items-start gap-3 px-2.5 pb-2 text-[12px] text-faint">
            <DeviceIcon className="mt-0.5 h-4 w-4 shrink-0" />
            <span className="leading-relaxed">
              Chưa đồng bộ thiết bị nào — ứng dụng chưa có máy chủ tài khoản.
            </span>
          </div>
        </div>
      )}
    </div>
  )
}


export default BrowserMenu
