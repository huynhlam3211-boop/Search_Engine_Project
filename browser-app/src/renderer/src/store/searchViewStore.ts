import { create } from './createStore'
import { search, type SearchResultDto } from '../lib/searchApi'

export type SearchStatus = 'idle' | 'loading' | 'done' | 'error'

interface SearchViewStore {
  query: string
  results: SearchResultDto[]
  total: number
  tookMs: number
  status: SearchStatus
  error: string | null
  usedMock: boolean
  /** Chi doi chu trong o dia chi, khong goi API. */
  setQuery: (query: string) => void
  runSearch: (query: string) => Promise<void>
  clear: () => void
}

export const useSearchViewStore = create<SearchViewStore>((set) => ({
  query: '',
  results: [],
  total: 0,
  tookMs: 0,
  status: 'idle',
  error: null,
  usedMock: false,

  setQuery: (query) => set({ query }),

  runSearch: async (query) => {
    const trimmed = query.trim()
    if (!trimmed) {
      set({ query: '', results: [], total: 0, status: 'idle', error: null })
      return
    }
    set({ query: trimmed, status: 'loading', error: null })
    try {
      const res = await search(trimmed)
      // Bo qua ket qua ve muon neu nguoi dung da go truy van khac.
      if (useSearchViewStore.getState().query !== trimmed) return
      set({
        results: res.items,
        total: res.total,
        tookMs: res.tookMs,
        usedMock: res.mock,
        status: 'done'
      })
    } catch (e) {
      set({ status: 'error', error: e instanceof Error ? e.message : String(e), results: [] })
    }
  },

  clear: () => set({ query: '', results: [], total: 0, status: 'idle', error: null })
}))
