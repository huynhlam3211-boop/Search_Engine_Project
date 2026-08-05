import { useState, type JSX } from 'react'
import NavigationButtons from './NavigationButtons'
import AddressBar from './AddressBar'
import BrowserMenu from './BrowserMenu'
import { MenuIcon } from './icon'

function Toolbar(): JSX.Element {
  const [menuOpen, setMenuOpen] = useState(false)

  return (
    <div className="toolbar">
      <NavigationButtons />
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

export default Toolbar
