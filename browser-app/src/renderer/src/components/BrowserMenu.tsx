import type { JSX } from 'react'
import Popover from './Popover'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { useSidePanelStore } from '../store/sidePanelStore'
import { ACCOUNT } from '../lib/account'

interface Props {
  open: boolean
  onClose: () => void
}

function BrowserMenu({ open, onClose }: Props): JSX.Element {
  const createTab = useTabStore((s) => s.createTab)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const toggle = useSidePanelStore((s) => s.toggle)

  const run = (fn: () => void): (() => void) => () => {
    fn()
    onClose()
  }

  return (
    <Popover open={open} onClose={onClose}>
      
    </Popover>
  )
}

export default BrowserMenu
