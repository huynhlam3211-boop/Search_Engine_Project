import { create } from './createStore'

export const PANEL_WIDTH = 320

export type PanelTab = 'bookmarks' | 'history' | 'about'

interface SidePanelStore {
  open: boolean
  tab: PanelTab
  toggle: (tab: PanelTab) => void
  close: () => void
}

export const useSidePanelStore = create<SidePanelStore>((set, get) => ({
  open: false,
  tab: 'bookmarks',

  // Bam lai dung muc dang mo thi dong panel, bam muc khac thi doi noi dung.
  toggle: (tab) => {
    const { open, tab: current } = get()
    if (open && current === tab) set({ open: false })
    else set({ open: true, tab })
  },

  close: () => set({ open: false })
}))
