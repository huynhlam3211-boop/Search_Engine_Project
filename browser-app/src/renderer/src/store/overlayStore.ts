import { create } from './createStore'

/**
 * Dem so lop noi (menu, dropdown goi y...) dang mo.
 * Khi count > 0 thi main process an trang web di, neu khong lop noi se bi
 * WebContentsView cua trang web che mat.
 * Dung bo dem thay vi mot co boolean vi co the co nhieu lop mo cung luc.
 */
interface OverlayStore {
  count: number
  open: () => void
  close: () => void
}

export const useOverlayStore = create<OverlayStore>((set, get) => ({
  count: 0,
  open: () => set({ count: get().count + 1 }),
  close: () => set({ count: Math.max(0, get().count - 1) })
}))
