const LOCALE = 'en-US'

export function count(value: number): string {
  if (!Number.isFinite(value)) {
    return '—'
  }
  return Math.round(value).toLocaleString(LOCALE)
}

export function compact(value: number): string {
  if (!Number.isFinite(value)) {
    return '—'
  }
  const absolute = Math.abs(value)
  if (absolute >= 1_000_000) {
    return `${(value / 1_000_000).toLocaleString(LOCALE, { maximumFractionDigits: 1 })} M`
  }
  if (absolute >= 10_000) {
    return `${(value / 1_000).toLocaleString(LOCALE, { maximumFractionDigits: 1 })} K`
  }
  return count(value)
}

export function percent(ratio: number, digits = 1): string {
  if (!Number.isFinite(ratio)) {
    return '—'
  }
  return `${(ratio * 100).toLocaleString(LOCALE, {
    minimumFractionDigits: digits,
    maximumFractionDigits: digits
  })}%`
}

export function bytes(value: number): string {
  if (!Number.isFinite(value) || value < 0) {
    return '—'
  }
  const units = ['B', 'KB', 'MB', 'GB']
  let size = value
  let unit = 0
  while (size >= 1024 && unit < units.length - 1) {
    size /= 1024
    unit++
  }
  const digits = unit === 0 ? 0 : 1
  return `${size.toLocaleString(LOCALE, { maximumFractionDigits: digits })} ${units[unit]}`
}

export function millis(value: number): string {
  if (!Number.isFinite(value)) {
    return '—'
  }
  if (value > 0 && value < 1) {
    return '< 1 ms'
  }
  return `${Math.round(value).toLocaleString(LOCALE)} ms`
}

export function dateTime(iso: string | null | undefined): string {
  if (!iso) {
    return '—'
  }
  const parsed = new Date(iso)
  if (Number.isNaN(parsed.getTime())) {
    return '—'
  }
  return parsed.toLocaleString(LOCALE, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

export function dayLabel(isoDate: string): string {
  const parts = isoDate.split('-')
  return parts.length === 3 ? `${parts[1]}/${parts[2]}` : isoDate
}

export function shortUrl(url: string, maxChars = 64): string {
  const stripped = url.replace(/^https?:\/\//, '').replace(/^www\./, '')
  if (stripped.length <= maxChars) {
    return stripped
  }
  const head = Math.ceil((maxChars - 1) / 2)
  const tail = Math.floor((maxChars - 1) / 2)
  return `${stripped.slice(0, head)}…${stripped.slice(stripped.length - tail)}`
}
