import type { JSX } from 'react'
import { useSidePanelStore, PANEL_WIDTH } from '../store/sidePanelStore'
import { useBookmarkStore } from '../store/bookmarkStore'
import { useHistoryStore } from '../store/historyStore'
import { useTabStore } from '../store/tabStore'
import { API_BASE } from '../lib/searchApi'
import { CloseIcon } from './icon'

const TITLES = {
  bookmarks: 'Dấu trang',
  history: 'Lịch sử',
  about: 'Giới thiệu'
} as const

function timeLabel(ts: number): string {
  return new Date(ts).toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' })
}

function SidePanel(): JSX.Element | null {
  const open = useSidePanelStore((s) => s.open)
  const tab = useSidePanelStore((s) => s.tab)
  const close = useSidePanelStore((s) => s.close)
  const bookmarks = useBookmarkStore((s) => s.items)
  const removeBookmark = useBookmarkStore((s) => s.remove)
  const history = useHistoryStore((s) => s.entries)
  const clearHistory = useHistoryStore((s) => s.clear)
  const navigate = useTabStore((s) => s.navigate)

  if (!open) return null

  return (
    <aside className="panel" style={{ width: PANEL_WIDTH }}>
      
    </aside>
  )
}

export default SidePanel
