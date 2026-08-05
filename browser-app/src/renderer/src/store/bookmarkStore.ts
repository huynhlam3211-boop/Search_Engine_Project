import { create } from './createStore'
import { BookmarkTrie, type Bookmark } from '../lib/BookmarkTrie'

const STORAGE_KEY = 'vnsearch.bookmarks'

function load(): Bookmark[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? (JSON.parse(raw) as Bookmark[]) : []
  } catch {
    return []
  }
}

function persist(items: Bookmark[]): void {
  try {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(items))
  } catch {
    // Het dung luong localStorage thi bo qua, khong lam hong giao dien.
  }
}

interface BookmarkStore {
  items: Bookmark[]
  /** Trie duoc dung lai moi khi items doi; component doc `root` de re-render. */
  root: BookmarkTrie
  isBookmarked: (url: string) => boolean
  toggleBookmark: (url: string, title?: string) => void
  remove: (url: string) => void
  searchByPrefix: (prefix: string) => Bookmark[]
}

export const useBookmarkStore = create<BookmarkStore>((set, get) => {
  const initial = load()

  const commit = (items: Bookmark[]): void => {
    persist(items)
    set({ items, root: BookmarkTrie.from(items) })
  }

  return {
    items: initial,
    root: BookmarkTrie.from(initial),

    isBookmarked: (url) => get().items.some((b) => b.url === url),

    toggleBookmark: (url, title) => {
      const items = get().items
      commit(
        items.some((b) => b.url === url)
          ? items.filter((b) => b.url !== url)
          : [...items, { url, title: title?.trim() || url }]
      )
    },

    remove: (url) => commit(get().items.filter((b) => b.url !== url)),

    searchByPrefix: (prefix) => get().root.searchByPrefix(prefix)
  }
})
