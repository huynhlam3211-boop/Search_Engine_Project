import { create } from 'zustand'

export type SearchMode = 'web' | 'images'

interface SearchViewState {
  query: string | null
  mode: SearchMode
  runSearch: (query: string) => void
  setMode: (mode: SearchMode) => void
  clear: () => void
}

export const useSearchViewStore = create<SearchViewState>((set) => ({
  query: null,
  mode: 'web',

  runSearch: (query) => {
    const trimmed = query.trim()
    set({ query: trimmed || null, mode: 'web' })
  },

  setMode: (mode) => set({ mode }),

  clear: () => set({ query: null, mode: 'web' })
}))
