import { useState, type JSX, type ReactNode } from 'react'
import { count as fmtCount, compact } from '../../lib/format'

const SERIES_COLORS = [
  'var(--color-viz-1)',
  'var(--color-viz-2)',
  'var(--color-viz-3)',
  'var(--color-viz-4)'
] as const

function niceMax(value: number): number {
  if (!Number.isFinite(value) || value <= 0) {
    return 1
  }
  const magnitude = 10 ** Math.floor(Math.log10(value))
  const normalized = value / magnitude
  const step = normalized <= 1 ? 1 : normalized <= 2 ? 2 : normalized <= 5 ? 5 : 10
  return step * magnitude
}

export function StatTile({
  label,
  value,
  hint,
  accent
}: {
  label: string
  value: string
  hint?: string
  accent?: number
}): JSX.Element {
  return (
    <div className="rounded-2xl border border-line bg-surface px-4 py-3.5">
      <div className="flex items-center gap-1.5">
        {accent !== undefined && (
          <span
            className="h-2 w-2 shrink-0 rounded-full"
            style={{ background: SERIES_COLORS[accent % SERIES_COLORS.length] }}
            aria-hidden="true"
          />
        )}
        <span className="truncate text-[12px] text-muted">{label}</span>
      </div>
      <p className="mt-1 text-[26px] font-semibold leading-none text-ink">{value}</p>
      {hint && <p className="mt-1.5 text-[11px] leading-snug text-faint">{hint}</p>}
    </div>
  )
}

export function ChartCard({
  title,
  subtitle,
  action,
  children
}: {
  title: string
  subtitle?: string
  action?: ReactNode
  children: ReactNode
}): JSX.Element {
  return (
    <section className="rounded-2xl border border-line bg-surface p-4">
      <header className="mb-3 flex items-start gap-3">
        <div className="min-w-0 flex-1">
          <h3 className="text-[13px] font-semibold text-ink">{title}</h3>
          {subtitle && <p className="mt-0.5 text-[11.5px] leading-snug text-faint">{subtitle}</p>}
        </div>
        {action}
      </header>
      {children}
    </section>
  )
}

function EmptyNote({ text }: { text: string }): JSX.Element {
  return <p className="py-10 text-center text-[12px] text-faint">{text}</p>
}

export interface TrendSeries {
  name: string
  values: number[]
}

const PLOT = { width: 720, height: 200, left: 44, right: 14, top: 12, bottom: 24 }

export function TrendChart({
  labels,
  series,
  emptyText
}: {
  labels: string[]
  series: TrendSeries[]
  emptyText: string
}): JSX.Element {
  const [active, setActive] = useState<number | null>(null)
  const [showTable, setShowTable] = useState(false)

  const points = labels.length
  const total = series.reduce((sum, s) => sum + s.values.reduce((a, b) => a + b, 0), 0)
  if (points === 0 || total === 0) {
    return <EmptyNote text={emptyText} />
  }

  const maxValue = niceMax(Math.max(...series.flatMap((s) => s.values)))
  const plotWidth = PLOT.width - PLOT.left - PLOT.right
  const plotHeight = PLOT.height - PLOT.top - PLOT.bottom
  const x = (index: number): number =>
    PLOT.left + (points === 1 ? plotWidth / 2 : (index / (points - 1)) * plotWidth)
  const y = (value: number): number => PLOT.top + plotHeight - (value / maxValue) * plotHeight

  const ticks = [0, 0.25, 0.5, 0.75, 1].map((fraction) => fraction * maxValue)
  const labelStep = Math.max(1, Math.ceil(points / 6))

  const leader = series.reduce(
    (best, s, index) => (s.values[points - 1] > series[best].values[points - 1] ? index : best),
    0
  )

  function pick(clientX: number, element: SVGSVGElement): void {
    const box = element.getBoundingClientRect()
    const ratio = (clientX - box.left) / box.width
    const viewX = ratio * PLOT.width
    const index = Math.round(((viewX - PLOT.left) / plotWidth) * (points - 1))
    setActive(Math.min(points - 1, Math.max(0, index)))
  }

  return (
    <div>
      <div className="mb-2 flex flex-wrap items-center gap-x-4 gap-y-1">
        {series.map((s, index) => (
          <span key={s.name} className="flex items-center gap-1.5 text-[11.5px] text-muted">
            <span
              className="h-[3px] w-4 rounded-full"
              style={{ background: SERIES_COLORS[index % SERIES_COLORS.length] }}
              aria-hidden="true"
            />
            {s.name}
          </span>
        ))}
        <button
          onClick={() => setShowTable((open) => !open)}
          className="ml-auto rounded-full border border-line px-2.5 py-1 text-[11px] text-muted
                     transition hover:bg-raised hover:text-ink focus-visible:outline-none
                     focus-visible:ring-2 focus-visible:ring-brand/50"
          aria-pressed={showTable}
        >
          {showTable ? 'Hide data table' : 'Data table'}
        </button>
      </div>

      <div className="relative">
        <svg
          viewBox={`0 0 ${PLOT.width} ${PLOT.height}`}
          className="h-auto w-full"
          role="img"
          aria-label={`Line chart: ${series.map((s) => s.name).join(', ')} by hour`}
          tabIndex={0}
          onPointerMove={(event) => pick(event.clientX, event.currentTarget)}
          onPointerLeave={() => setActive(null)}
          onKeyDown={(event) => {
            if (event.key === 'ArrowRight') {
              setActive((current) => Math.min(points - 1, (current ?? -1) + 1))
            } else if (event.key === 'ArrowLeft') {
              setActive((current) => Math.max(0, (current ?? points) - 1))
            } else if (event.key === 'Escape') {
              setActive(null)
            }
          }}
        >
          {ticks.map((tick) => (
            <g key={tick}>
              <line
                x1={PLOT.left}
                x2={PLOT.width - PLOT.right}
                y1={y(tick)}
                y2={y(tick)}
                className="stroke-line"
                strokeWidth={1}
              />
              <text
                x={PLOT.left - 8}
                y={y(tick) + 3.5}
                textAnchor="end"
                className="fill-faint text-[10px] [font-variant-numeric:tabular-nums]"
              >
                {compact(tick)}
              </text>
            </g>
          ))}

          {labels.map((label, index) =>
            index % labelStep === 0 || index === points - 1 ? (
              <text
                key={label + index}
                x={x(index)}
                y={PLOT.height - 6}
                textAnchor="middle"
                className="fill-faint text-[10px] [font-variant-numeric:tabular-nums]"
              >
                {label}
              </text>
            ) : null
          )}

          {series.map((s, index) => {
            const color = SERIES_COLORS[index % SERIES_COLORS.length]
            const path = s.values
              .map((value, i) => `${i === 0 ? 'M' : 'L'}${x(i)},${y(value)}`)
              .join(' ')
            return (
              <g key={s.name}>
                <path
                  d={path}
                  fill="none"
                  stroke={color}
                  strokeWidth={2}
                  strokeLinecap="round"
                  strokeLinejoin="round"
                />
                <circle
                  cx={x(points - 1)}
                  cy={y(s.values[points - 1])}
                  r={4}
                  fill={color}
                  className="stroke-surface"
                  strokeWidth={2}
                />
                {index === leader && (
                  <text
                    x={x(points - 1) - 8}
                    y={y(s.values[points - 1]) - 8}
                    textAnchor="end"
                    className="fill-muted text-[10.5px] font-medium"
                  >
                    {fmtCount(s.values[points - 1])}
                  </text>
                )}
              </g>
            )
          })}

          {active !== null && (
            <g pointerEvents="none">
              <line
                x1={x(active)}
                x2={x(active)}
                y1={PLOT.top}
                y2={PLOT.top + plotHeight}
                className="stroke-faint"
                strokeWidth={1}
              />
              {series.map((s, index) => (
                <circle
                  key={s.name}
                  cx={x(active)}
                  cy={y(s.values[active])}
                  r={4}
                  fill={SERIES_COLORS[index % SERIES_COLORS.length]}
                  className="stroke-surface"
                  strokeWidth={2}
                />
              ))}
            </g>
          )}
        </svg>

        {active !== null && (
          <div
            className="pointer-events-none absolute top-1 z-10 -translate-x-1/2 rounded-lg border
                       border-line bg-surface px-2.5 py-1.5 shadow-pop"
            style={{ left: `${(x(active) / PLOT.width) * 100}%` }}
            role="status"
          >
            <p className="text-[11px] font-medium text-ink">{labels[active]}</p>
            {series.map((s, index) => (
              <p key={s.name} className="mt-0.5 flex items-center gap-1.5 text-[11px] text-muted">
                <span
                  className="h-1.5 w-1.5 rounded-full"
                  style={{ background: SERIES_COLORS[index % SERIES_COLORS.length] }}
                  aria-hidden="true"
                />
                {s.name}
                <span className="ml-auto pl-2 font-medium text-ink">
                  {fmtCount(s.values[active])}
                </span>
              </p>
            ))}
          </div>
        )}
      </div>

      {showTable && (
        <div className="mt-3 max-h-56 overflow-y-auto rounded-xl border border-line">
          <table className="w-full text-left text-[11.5px]">
            <thead className="sticky top-0 bg-raised text-muted">
              <tr>
                <th className="px-3 py-1.5 font-medium">Hour</th>
                {series.map((s) => (
                  <th key={s.name} className="px-3 py-1.5 text-right font-medium">
                    {s.name}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody className="[font-variant-numeric:tabular-nums]">
              {labels.map((label, index) => (
                <tr key={label + index} className="border-t border-line">
                  <td className="px-3 py-1 text-muted">{label}</td>
                  {series.map((s) => (
                    <td key={s.name} className="px-3 py-1 text-right text-ink">
                      {fmtCount(s.values[index])}
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}

export function ColumnChart({
  data,
  emptyText,
  labelEvery = 1
}: {
  data: { label: string; value: number }[]
  emptyText: string
  labelEvery?: number
}): JSX.Element {
  if (data.length === 0 || data.every((item) => item.value === 0)) {
    return <EmptyNote text={emptyText} />
  }

  const width = 720
  const height = 170
  const padding = { left: 40, right: 10, top: 14, bottom: 26 }
  const plotWidth = width - padding.left - padding.right
  const plotHeight = height - padding.top - padding.bottom
  const maxValue = niceMax(Math.max(...data.map((item) => item.value)))
  const band = plotWidth / data.length
  const barWidth = Math.min(24, band * 0.62)
  const peak = data.reduce((best, item, index) => (item.value > data[best].value ? index : best), 0)

  return (
    <svg
      viewBox={`0 0 ${width} ${height}`}
      className="h-auto w-full"
      role="img"
      aria-label="Bar chart"
    >
      {[0, 0.5, 1].map((fraction) => {
        const value = fraction * maxValue
        const y = padding.top + plotHeight - fraction * plotHeight
        return (
          <g key={fraction}>
            <line
              x1={padding.left}
              x2={width - padding.right}
              y1={y}
              y2={y}
              className="stroke-line"
              strokeWidth={1}
            />
            <text
              x={padding.left - 8}
              y={y + 3.5}
              textAnchor="end"
              className="fill-faint text-[10px] [font-variant-numeric:tabular-nums]"
            >
              {compact(value)}
            </text>
          </g>
        )
      })}

      {data.map((item, index) => {
        const barHeight = (item.value / maxValue) * plotHeight
        const x = padding.left + band * index + (band - barWidth) / 2
        const y = padding.top + plotHeight - barHeight
        const radius = Math.min(4, barHeight / 2)
        return (
          <g key={item.label + index}>
            <path
              d={
                barHeight <= 0
                  ? ''
                  : `M${x},${y + barHeight} L${x},${y + radius} Q${x},${y} ${x + radius},${y} ` +
                    `L${x + barWidth - radius},${y} Q${x + barWidth},${y} ${x + barWidth},${
                      y + radius
                    } L${x + barWidth},${y + barHeight} Z`
              }
              fill={SERIES_COLORS[0]}
            >
              <title>{`${item.label}: ${fmtCount(item.value)}`}</title>
            </path>
            {index === peak && item.value > 0 && (
              <text
                x={x + barWidth / 2}
                y={y - 5}
                textAnchor="middle"
                className="fill-muted text-[10.5px] font-medium"
              >
                {fmtCount(item.value)}
              </text>
            )}
            {(index % labelEvery === 0 || index === data.length - 1) && (
              <text
                x={x + barWidth / 2}
                y={height - 8}
                textAnchor="middle"
                className="fill-faint text-[9.5px]"
              >
                {item.label}
              </text>
            )}
          </g>
        )
      })}
    </svg>
  )
}

export function BarList({
  rows,
  emptyText
}: {
  rows: { label: string; value: number; sub?: string; title?: string }[]
  emptyText: string
}): JSX.Element {
  if (rows.length === 0) {
    return <EmptyNote text={emptyText} />
  }
  const maxValue = Math.max(...rows.map((row) => row.value), 1)

  return (
    <ol className="flex flex-col gap-2.5">
      {rows.map((row, index) => (
        <li key={row.label + index} title={row.title ?? row.label}>
          <div className="flex items-baseline gap-2">
            <span className="w-4 shrink-0 text-[11px] text-faint [font-variant-numeric:tabular-nums]">
              {index + 1}
            </span>
            <span className="min-w-0 flex-1 truncate text-[12.5px] text-ink">{row.label}</span>
            {row.sub && <span className="shrink-0 text-[11px] text-faint">{row.sub}</span>}
            <span className="shrink-0 text-[12px] font-medium text-ink [font-variant-numeric:tabular-nums]">
              {fmtCount(row.value)}
            </span>
          </div>
          <div className="ml-6 mt-1 h-1.5 overflow-hidden rounded-full bg-raised">
            <div
              className="h-full rounded-full"
              style={{
                width: `${Math.max(2, (row.value / maxValue) * 100)}%`,
                background: SERIES_COLORS[0]
              }}
            />
          </div>
        </li>
      ))}
    </ol>
  )
}

export function ShareBar({
  parts,
  emptyText
}: {
  parts: { label: string; value: number }[]
  emptyText: string
}): JSX.Element {
  const total = parts.reduce((sum, part) => sum + part.value, 0)
  if (total === 0) {
    return <EmptyNote text={emptyText} />
  }

  return (
    <div>
      <div className="flex h-3 w-full gap-[2px] overflow-hidden rounded-full">
        {parts.map((part, index) => (
          <div
            key={part.label}
            style={{
              width: `${(part.value / total) * 100}%`,
              background: SERIES_COLORS[index % SERIES_COLORS.length]
            }}
            title={`${part.label}: ${fmtCount(part.value)}`}
          />
        ))}
      </div>
      <ul className="mt-3 flex flex-col gap-1.5">
        {parts.map((part, index) => (
          <li key={part.label} className="flex items-center gap-2 text-[12px]">
            <span
              className="h-2 w-2 shrink-0 rounded-full"
              style={{ background: SERIES_COLORS[index % SERIES_COLORS.length] }}
              aria-hidden="true"
            />
            <span className="min-w-0 flex-1 truncate text-muted">{part.label}</span>
            <span className="text-ink [font-variant-numeric:tabular-nums]">
              {fmtCount(part.value)}
            </span>
            <span className="w-12 text-right text-faint [font-variant-numeric:tabular-nums]">
              {((part.value / total) * 100).toFixed(1)}%
            </span>
          </li>
        ))}
      </ul>
    </div>
  )
}
