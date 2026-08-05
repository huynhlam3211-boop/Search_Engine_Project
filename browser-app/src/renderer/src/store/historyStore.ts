import { create } from './createStore'

const LIMIT = 200

export interface HistoryEntry {
  url: string
  title: string
  visitedAt: number
}

interface HistoryStore {
  entries: HistoryEntry[]
  add: (url: string, title: string) => void
  clear: () => void
}

export const useHistoryStore = create<HistoryStore>((set, get) => ({
  entries: [],

  add: (url, title) => {
    const entries = get().entries
    // Tai lai cung mot trang thi chi cap nhat lan truy cap gan nhat.
    if (entries[0]?.url === url) return
    set({ entries: [{ url, title, visitedAt: Date.now() }, ...entries].slice(0, LIMIT) })
  },

  clear: () => set({ entries: [] })
}))
