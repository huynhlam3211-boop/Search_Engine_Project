import type { JSX } from 'react'
import { GlobeIcon, SearchIcon } from './icon'

interface Props {
  items: string[]
  highlighted: number
  onPick: (value: string) => void
  onHover: (index: number) => void
}

function AutocompleteDropdown({ items, highlighted, onPick, onHover }: Props): JSX.Element | null {
  if (items.length === 0) return null

  return (
    <ul className="acdrop" role="listbox">
      
    </ul>
  )
}

export default AutocompleteDropdown
