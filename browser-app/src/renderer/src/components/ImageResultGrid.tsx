import { useCallback, useEffect, useLayoutEffect, useMemo, useRef, useState, type JSX } from 'react'
import { searchImages, type ImageResultDto } from '../lib/searchApi'
import { useSearchViewStore } from '../store/searchViewStore'
import { useTabStore } from '../store/tabStore'
import { hostOf, siteGradient, siteInitial } from '../lib/site'
import {
  AlertIcon,
  ChevronLeftIcon,
  ChevronRightIcon,
  CloseIcon,
  GlobeIcon,
  SearchIcon,
  SpinnerIcon
} from './icons'

const BATCH_SIZE = 24

const PREFETCH_MARGIN = '400px'

const TARGET_ROW_HEIGHT = 180

const GAP = 8

const FALLBACK_RATIO = 4 / 3

const MIN_RATIO = 0.4
const MAX_RATIO = 3.0

interface Props {
  onMeta?: (meta: ImageMeta) => void
}

export interface ImageMeta {
  query: string
  total: number
  timeTakenMs: number
}

interface PlacedImage {
  image: ImageResultDto
  index: number
  width: number
}

interface Row {
  items: PlacedImage[]
  height: number
}

function buildRows(
  images: ImageResultDto[],
  ratioOf: (image: ImageResultDto) => number,
  width: number,
  target: number,
  gap: number
): Row[] {
  const rows: Row[] = []
  if (width <= 0) {
    return rows
  }

  let current: ImageResultDto[] = []
  let sumRatio = 0

  let placed = 0

  const flush = (height: number): void => {
    rows.push({
      items: current.map((image, i) => ({
        image,
        index: placed + i,
        width: ratioOf(image) * height
      })),
      height
    })
    placed += current.length
    current = []
    sumRatio = 0
  }

  for (const image of images) {
    const ratio = ratioOf(image)

    const heightWithout =
      current.length > 0 ? (width - gap * (current.length - 1)) / sumRatio : Infinity

    current.push(image)
    sumRatio += ratio

    const heightWith = (width - gap * (current.length - 1)) / sumRatio
    if (heightWith > target) {
      continue
    }

    if (heightWithout - target < target - heightWith) {
      current.pop()
      sumRatio -= ratio
      flush(heightWithout)
      current.push(image)
      sumRatio = ratio
    } else {
      flush(heightWith)
    }
  }

  if (current.length > 0) {
    const available = width - gap * (current.length - 1)
    flush(Math.min(target, available / sumRatio))
  }

  return rows
}

function useContainerWidth(): [React.RefObject<HTMLDivElement | null>, number] {
  const ref = useRef<HTMLDivElement>(null)
  const [width, setWidth] = useState(0)

  useLayoutEffect(() => {
    const element = ref.current
    if (!element) {
      return undefined
    }
    setWidth(element.clientWidth)

    const observer = new ResizeObserver((entries) => {
      const next = entries[0]?.contentRect.width ?? 0
      setWidth((prev) => (Math.abs(prev - next) < 1 ? prev : next))
    })
    observer.observe(element)
    return () => observer.disconnect()
  }, [])

  return [ref, width]
}

function ImageResultGrid({ onMeta }: Props): JSX.Element {
  const query = useSearchViewStore((state) => state.query)
  const clearSearch = useSearchViewStore((state) => state.clear)
  const navigate = useTabStore((state) => state.navigate)

  const [images, setImages] = useState<ImageResultDto[]>([])
  const [page, setPage] = useState(0)
  const [hasMore, setHasMore] = useState(true)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [broken, setBroken] = useState<Set<string>>(new Set())

  const [measured, setMeasured] = useState<Map<string, number>>(new Map())

  const [selected, setSelected] = useState<number | null>(null)

  const [gridRef, width] = useContainerWidth()
  const sentinelRef = useRef<HTMLDivElement>(null)

  const inFlight = useRef(false)

  const loadNext = useCallback(async () => {
    if (!query || inFlight.current || !hasMore) {
      return
    }
    inFlight.current = true
    setLoading(true)

    const next = page + 1
    try {
      const response = await searchImages(query, next, BATCH_SIZE)
      setImages((prev) => {
        const seen = new Set(prev.map((image) => image.imageUrl))
        return [...prev, ...response.results.filter((image) => !seen.has(image.imageUrl))]
      })
      setHasMore(response.hasMore)
      setPage(next)
      setError(null)
      onMeta?.({ query, total: response.totalResults, timeTakenMs: response.timeTakenMs })
    } catch {
      setError(
        'Cannot reach the search server (http://localhost:8080). Make sure the backend is running.'
      )
      setHasMore(false)
      onMeta?.({ query, total: 0, timeTakenMs: 0 })
    } finally {
      inFlight.current = false
      setLoading(false)
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query, page, hasMore])

  useEffect(() => {
    const sentinel = sentinelRef.current
    if (!sentinel || !hasMore) {
      return undefined
    }
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0]?.isIntersecting) {
          void loadNext()
        }
      },
      { rootMargin: PREFETCH_MARGIN }
    )
    observer.observe(sentinel)
    return () => observer.disconnect()
  }, [loadNext, hasMore])

  const open = useCallback(
    (image: ImageResultDto): void => {
      navigate(image.pageUrl)
      clearSearch()
    },
    [navigate, clearSearch]
  )

  const ratioOf = useCallback(
    (image: ImageResultDto): number => {
      const fromMeasure = measured.get(image.imageUrl)
      const raw =
        fromMeasure ??
        (image.width > 0 && image.height > 0 ? image.width / image.height : FALLBACK_RATIO)
      return Math.min(MAX_RATIO, Math.max(MIN_RATIO, raw))
    },
    [measured]
  )

  const rows = useMemo(
    () => buildRows(images, ratioOf, width, TARGET_ROW_HEIGHT, GAP),
    [images, ratioOf, width]
  )

  const onImageLoad = useCallback((image: ImageResultDto, element: HTMLImageElement): void => {
    const { naturalWidth, naturalHeight } = element
    if (naturalWidth <= 0 || naturalHeight <= 0) {
      return
    }
    const actual = naturalWidth / naturalHeight
    setMeasured((prev) => {
      const current = prev.get(image.imageUrl)
      if (current !== undefined && Math.abs(current - actual) < 0.01) {
        return prev
      }
      const next = new Map(prev)
      next.set(image.imageUrl, actual)
      return next
    })
  }, [])

  useEffect(() => {
    if (selected === null) {
      return undefined
    }
    const onKey = (event: KeyboardEvent): void => {
      if (event.key === 'Escape') {
        setSelected(null)
      } else if (event.key === 'ArrowRight') {
        event.preventDefault()
        setSelected((prev) => (prev === null ? null : Math.min(images.length - 1, prev + 1)))
      } else if (event.key === 'ArrowLeft') {
        event.preventDefault()
        setSelected((prev) => (prev === null ? null : Math.max(0, prev - 1)))
      }
    }
    window.addEventListener('keydown', onKey)
    return () => window.removeEventListener('keydown', onKey)
  }, [selected, images.length])

  const empty = images.length === 0
  const firstLoad = empty && loading
  const nothingFound = empty && !loading && !hasMore && !error

  return (
    <>
      {empty && error && (
        <div className="flex items-start gap-3 rounded-2xl border border-danger/25 bg-danger/5 px-4 py-3.5">
          <AlertIcon className="mt-0.5 h-5 w-5 shrink-0 text-danger" />
          <p className="text-sm text-danger">{error}</p>
        </div>
      )}

      {nothingFound && <EmptyState />}

      <div ref={gridRef}>
        {(firstLoad || (empty && width === 0 && !error)) && <GridSkeleton />}

        {rows.map((row, rowIndex) => {
          const holdsSelected =
            selected !== null &&
            row.items.length > 0 &&
            selected >= row.items[0].index &&
            selected <= row.items[row.items.length - 1].index

          return (
            <div key={rowIndex}>
              <div className="flex" style={{ gap: GAP, marginBottom: GAP }}>
                {row.items.map((placed) => (
                  <ImageCell
                    key={placed.image.imageUrl}
                    placed={placed}
                    rowHeight={row.height}
                    active={selected === placed.index}
                    broken={broken.has(placed.image.imageUrl)}
                    onSelect={() =>
                      setSelected((prev) => (prev === placed.index ? null : placed.index))
                    }
                    onLoad={onImageLoad}
                    onBroken={() =>
                      setBroken((prev) => {
                        const next = new Set(prev)
                        next.add(placed.image.imageUrl)
                        return next
                      })
                    }
                  />
                ))}
              </div>

              {holdsSelected && selected !== null && (
                <ImagePreview
                  image={images[selected]}
                  position={selected - row.items[0].index}
                  rowLength={row.items.length}
                  broken={broken.has(images[selected].imageUrl)}
                  hasPrev={selected > 0}
                  hasNext={selected < images.length - 1}
                  onPrev={() => setSelected((prev) => (prev === null ? null : prev - 1))}
                  onNext={() => setSelected((prev) => (prev === null ? null : prev + 1))}
                  onClose={() => setSelected(null)}
                  onOpen={() => open(images[selected])}
                />
              )}
            </div>
          )
        })}
      </div>

      <div ref={sentinelRef} aria-hidden className="h-1" />

      {loading && images.length > 0 && (
        <div className="flex items-center justify-center gap-2 py-6 text-[13px] text-muted">
          <SpinnerIcon className="h-4 w-4" />
          Loading more images…
        </div>
      )}

      {!hasMore && !loading && images.length > 0 && (
        <p className="py-6 text-center text-[12px] text-faint">
          All {images.length.toLocaleString('en-US')} matching images are shown.
        </p>
      )}

      {error && images.length > 0 && (
        <div className="flex items-center justify-center gap-2 py-4 text-[12px] text-danger">
          <AlertIcon className="h-4 w-4" />
          Could not load more images.
        </div>
      )}
    </>
  )
}

interface CellProps {
  placed: PlacedImage
  rowHeight: number
  active: boolean
  broken: boolean
  onSelect: () => void
  onLoad: (image: ImageResultDto, element: HTMLImageElement) => void
  onBroken: () => void
}

function ImageCell({
  placed,
  rowHeight,
  active,
  broken,
  onSelect,
  onLoad,
  onBroken
}: CellProps): JSX.Element {
  const { image } = placed

  return (
    <button
      onClick={onSelect}
      title={`${image.pageTitle}\n${image.pageUrl}`}
      aria-pressed={active}
      style={{ width: placed.width, height: rowHeight }}
      className={`group relative shrink-0 overflow-hidden rounded-lg bg-raised text-left
                  transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50
                  ${active ? 'ring-2 ring-brand' : 'hover:brightness-95'}`}
    >
      {broken ? (
        <div className="flex h-full w-full flex-col items-center justify-center gap-1.5 px-2 text-center">
          <GlobeIcon className="h-5 w-5 text-faint" />
          <span className="line-clamp-2 text-[11px] leading-snug text-faint">
            {image.altText || 'Image failed to load'}
          </span>
        </div>
      ) : (
        <img
          src={image.imageUrl}
          alt={image.altText}
          loading="lazy"
          decoding="async"
          referrerPolicy="no-referrer"
          onLoad={(event) => onLoad(image, event.currentTarget)}
          onError={onBroken}
          className="h-full w-full object-cover"
        />
      )}

      {image.missingAlt && (
        <span
          title="This image has no alternative text (alt) — screen readers will skip it"
          className="absolute right-1.5 top-1.5 rounded-full bg-danger/90 px-1.5 py-0.5
                     text-[9px] font-semibold text-white shadow-sm"
        >
          missing alt
        </span>
      )}

      <span
        className="pointer-events-none absolute inset-x-0 bottom-0 flex items-center gap-1.5
                   bg-gradient-to-t from-black/80 to-transparent px-2 pb-1.5 pt-6
                   opacity-0 transition group-hover:opacity-100"
      >
        <span
          className="flex h-3.5 w-3.5 shrink-0 items-center justify-center rounded-full text-[7px] font-bold text-white"
          style={{ background: siteGradient(image.pageUrl) }}
        >
          {siteInitial(image.pageUrl)}
        </span>
        <span className="truncate text-[10px] text-white/90">
          {image.host || hostOf(image.pageUrl)}
        </span>
      </span>
    </button>
  )
}

interface PreviewProps {
  image: ImageResultDto
  position: number
  rowLength: number
  broken: boolean
  hasPrev: boolean
  hasNext: boolean
  onPrev: () => void
  onNext: () => void
  onClose: () => void
  onOpen: () => void
}

function ImagePreview({
  image,
  position,
  rowLength,
  broken,
  hasPrev,
  hasNext,
  onPrev,
  onNext,
  onClose,
  onOpen
}: PreviewProps): JSX.Element {
  const ref = useRef<HTMLDivElement>(null)

  useEffect(() => {
    ref.current?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
  }, [image.imageUrl])

  const host = image.host || hostOf(image.pageUrl)
  const hasSize = image.width > 0 && image.height > 0

  return (
    <div
      ref={ref}
      className="animate-fade-up relative mb-2 rounded-xl border border-line bg-raised"
      style={{ marginTop: -GAP + 2 }}
    >
      <span
        aria-hidden
        className="absolute -top-[7px] h-3 w-3 rotate-45 border-l border-t border-line bg-raised"
        style={{ left: `calc(${((position + 0.5) / rowLength) * 100}% - 6px)` }}
      />

      <div className="flex flex-col gap-4 p-4 sm:flex-row">
        <div className="flex shrink-0 items-center justify-center sm:w-2/5">
          {broken ? (
            <div className="flex h-40 w-full flex-col items-center justify-center gap-2 rounded-lg bg-base text-center">
              <GlobeIcon className="h-7 w-7 text-faint" />
              <span className="px-4 text-[12px] text-faint">
                The origin server refused this image (403/404)
              </span>
            </div>
          ) : (
            <img
              src={image.imageUrl}
              alt={image.altText}
              referrerPolicy="no-referrer"
              className="max-h-72 w-auto max-w-full rounded-lg object-contain"
            />
          )}
        </div>

        <div className="flex min-w-0 flex-1 flex-col">
          <button
            onClick={onOpen}
            className="text-left text-[15px] font-medium leading-snug text-link hover:underline
                       focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
          >
            <span className="line-clamp-3">{image.pageTitle}</span>
          </button>

          <div className="mt-2 flex items-center gap-1.5">
            <span
              className="flex h-4 w-4 shrink-0 items-center justify-center rounded-full text-[8px] font-bold text-white"
              style={{ background: siteGradient(image.pageUrl) }}
            >
              {siteInitial(image.pageUrl)}
            </span>
            <span className="truncate text-[12px] text-muted">{host}</span>
          </div>

          {image.altText && (
            <p className="mt-3 line-clamp-3 text-[13px] leading-relaxed text-muted">
              {image.altText}
            </p>
          )}

          <dl className="mt-3 flex flex-wrap gap-x-5 gap-y-1 text-[11px] text-faint">
            {hasSize && (
              <div className="flex gap-1.5">
                <dt>Dimensions</dt>
                <dd className="tabular-nums text-ink">
                  {image.width}×{image.height}
                </dd>
              </div>
            )}
            <div className="flex min-w-0 gap-1.5">
              <dt className="shrink-0">Alternative text</dt>
              <dd className={image.missingAlt ? 'text-danger' : 'text-ink'}>
                {image.missingAlt ? 'missing' : 'present'}
              </dd>
            </div>
          </dl>

          <div className="mt-auto flex items-center gap-2 pt-4">
            <button
              onClick={onOpen}
              className="rounded-lg bg-brand px-3 py-1.5 text-[13px] font-medium text-white
                         transition hover:brightness-110
                         focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
            >
              Open the page with this image
            </button>

            <button
              onClick={onPrev}
              disabled={!hasPrev}
              aria-label="Previous image"
              className="rounded-lg border border-line p-1.5 text-muted transition
                         hover:bg-base disabled:opacity-30 disabled:hover:bg-transparent
                         focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
            >
              <ChevronLeftIcon className="h-4 w-4" />
            </button>
            <button
              onClick={onNext}
              disabled={!hasNext}
              aria-label="Next image"
              className="rounded-lg border border-line p-1.5 text-muted transition
                         hover:bg-base disabled:opacity-30 disabled:hover:bg-transparent
                         focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
            >
              <ChevronRightIcon className="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>

      <button
        onClick={onClose}
        aria-label="Close"
        className="absolute right-2 top-2 rounded-lg p-1.5 text-faint transition hover:bg-base hover:text-ink
                   focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
      >
        <CloseIcon className="h-4 w-4" />
      </button>
    </div>
  )
}

function EmptyState(): JSX.Element {
  return (
    <div className="flex flex-col items-center gap-2 py-20 text-center">
      <SearchIcon className="h-9 w-9 text-faint" />
      <p className="text-[15px] text-ink">No images found.</p>
      <p className="max-w-md text-[13px] text-muted">
        Images are only recorded during a crawl. If the &ldquo;All&rdquo; tab has results but this
        one does not, those pages were crawled without image collection — run a new crawl.
      </p>
    </div>
  )
}

function GridSkeleton(): JSX.Element {
  const rows = [
    [28, 19, 24, 29],
    [22, 31, 25, 22],
    [26, 23, 33, 18]
  ]
  return (
    <div>
      {rows.map((row, rowIndex) => (
        <div key={rowIndex} className="flex" style={{ gap: GAP, marginBottom: GAP }}>
          {row.map((percent, index) => (
            <div
              key={index}
              className="animate-pulse rounded-lg bg-raised"
              style={{
                height: TARGET_ROW_HEIGHT,
                width: `calc(${percent}% - ${(GAP * (row.length - 1)) / row.length}px)`
              }}
            />
          ))}
        </div>
      ))}
    </div>
  )
}

export default ImageResultGrid
