import { useEffect, useRef, type JSX, type ReactNode } from 'react'
import { useOverlayStore } from '../store/overlayStore'

interface Props {
  open: boolean
  onClose: () => void
  align?: 'left' | 'right'
  // Cho phep de trong: BrowserMenu chua viet noi dung ben trong Popover.
  children?: ReactNode
}

/**
 * Lop noi dung chung cho menu/dropdown. Ngoai viec dong khi bam ra ngoai hay
 * nhan Esc, no con bao cho overlayStore biet dang co lop noi — main process
 * dua vao do de an trang web, neu khong WebContentsView se de len tren.
 */
function Popover({ open, onClose, align = 'right', children }: Props): JSX.Element | null {
  const ref = useRef<HTMLDivElement>(null)
  const openOverlay = useOverlayStore((s) => s.open)
  const closeOverlay = useOverlayStore((s) => s.close)

  useEffect(() => {
    if (!open) return

    openOverlay()
    const onPointerDown = (e: MouseEvent): void => {
      if (!ref.current?.contains(e.target as Node)) onClose()
    }
    const onKeyDown = (e: KeyboardEvent): void => {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('mousedown', onPointerDown)
    document.addEventListener('keydown', onKeyDown)

    return () => {
      closeOverlay()
      document.removeEventListener('mousedown', onPointerDown)
      document.removeEventListener('keydown', onKeyDown)
    }
  }, [open, onClose, openOverlay, closeOverlay])

  if (!open) return null

  return (
    <div ref={ref} className={`popover popover--${align}`}>
      
    </div>
  )
}

export default Popover
