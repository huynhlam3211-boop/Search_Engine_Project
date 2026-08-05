import type { JSX, SVGProps } from 'react'

type IconProps = SVGProps<SVGSVGElement>

function Svg({ children, ...props }: IconProps): JSX.Element {
  return (
    <svg
      viewBox="0 0 24 24"
      width={16}
      height={16}
      fill="none"
      stroke="currentColor"
      strokeWidth={1.8}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      
    </svg>
  )
}

export function CloseIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function PlusIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function BackIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function ForwardIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function ReloadIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function HomeIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function GlobeIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function LockIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function SearchIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function StarIcon({ filled, ...props }: IconProps & { filled?: boolean }): JSX.Element {
  return (
    <Svg fill={filled ? 'currentColor' : 'none'} {...props}>
    </Svg>
  )
}

export function MenuIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function BookmarkIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function HistoryIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

export function InfoIcon(props: IconProps): JSX.Element {
  return (
    <Svg {...props}>
    </Svg>
  )
}

/** Logo chu V cua VnSearch. */
export function VnSearchMark(props: IconProps): JSX.Element {
  return (
    <svg viewBox="0 0 24 24" width={18} height={18} aria-hidden="true" {...props}>
      
    </svg>
  )
}

// --- Nut dieu khien cua so (frame: false nen phai tu ve) ---

export function WinMinimize(props: IconProps): JSX.Element {
  return (
    <Svg width={12} height={12} strokeWidth={1.4} {...props}>
      
    </Svg>
  )
}

export function WinMaximize({ maximized, ...props }: IconProps & { maximized?: boolean }): JSX.Element {
  return (
    <Svg width={12} height={12} strokeWidth={1.4} {...props}>
      
    </Svg>
  )
}

export function WinClose(props: IconProps): JSX.Element {
  return (
    <Svg width={12} height={12} strokeWidth={1.4} {...props}>
    </Svg>
  )
}
