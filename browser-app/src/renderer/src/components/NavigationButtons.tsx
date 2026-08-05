import type { JSX } from 'react'
import { useTabStore } from '../store/tabStore'
import { BackIcon, ForwardIcon, HomeIcon, ReloadIcon } from './icon'

function NavigationButtons(): JSX.Element {
  const tabs = useTabStore((s) => s.tabs)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const goBack = useTabStore((s) => s.goBack)
  const goForward = useTabStore((s) => s.goForward)
  const reload = useTabStore((s) => s.reload)
  const goHome = useTabStore((s) => s.goHome)

  const active = tabs.find((t) => t.id === activeTabId)

  return (
    <div className="navbtns">
    </div>
  )
}

export default NavigationButtons
