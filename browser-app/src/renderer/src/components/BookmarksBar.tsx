import type { JSX } from 'react'
import { useBookmarkStore } from '../store/bookmarkStore'
import { useTabStore } from '../store/tabStore'
import { SEED_SITES } from '../lib/seedSites'
import { BookmarkIcon, GlobeIcon } from './icon'

function BookmarksBar(): JSX.Element {
  const items = useBookmarkStore((s) => s.items)
  const navigate = useTabStore((s) => s.navigate)

  // Chua co dau trang nao thi hien tam vai trang goc cho thanh do trong.
  const showSeeds = items.length === 0

  return (
    <div className="bmbar">
      
    </div>
  )
}

export default BookmarksBar
