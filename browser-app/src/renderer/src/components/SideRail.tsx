import type { JSX } from 'react'
import { useSidePanelStore, type PanelTab } from '../store/sidePanelStore'
import { BookmarkIcon, HistoryIcon, InfoIcon } from './icon'

const ITEMS: { id: PanelTab; label: string; Icon: typeof BookmarkIcon }[] = [
  { id: 'bookmarks', label: 'Dấu trang', Icon: BookmarkIcon },
  { id: 'history', label: 'Lịch sử', Icon: HistoryIcon },
  { id: 'about', label: 'Giới thiệu', Icon: InfoIcon }
]

function SideRail(): JSX.Element {
  const open = useSidePanelStore((s) => s.open)
  const tab = useSidePanelStore((s) => s.tab)
  const toggle = useSidePanelStore((s) => s.toggle)

  return (
    <div className="siderail">
      {ITEMS.map(({ id, label, Icon }) => (
        <button
          key={id}
          className={`siderail__btn${open && tab === id ? ' siderail__btn--on' : ''}`}
          title={label}
          aria-label={label}
          onClick={() => toggle(id)}
        >
          <Icon width={18} height={18} />
        </button>
      ))}
    </div>
  )
}

export default SideRail
