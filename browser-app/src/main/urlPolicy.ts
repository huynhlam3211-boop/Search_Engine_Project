export const HOME_URL = 'vnsearch://home'
const ALLOWED_PROTOCOLS = new Set(['http:', 'https:'])

const ABSOLUTE_URL = /^[a-z][a-z0-9+.-]*:\/\//i

const SCHEME_WITHOUT_SLASHES = /^[a-z][a-z0-9+.-]*:(?!\d)/i

export function resolveNavigable(input: string | null | undefined): string | null {
  if (typeof input !== 'string') {
    return null
  }
  const trimmed = input.trim()
  if (!trimmed) {
    return null
  }
  if (trimmed === HOME_URL) {
    return HOME_URL
  }
  if (!ABSOLUTE_URL.test(trimmed) && SCHEME_WITHOUT_SLASHES.test(trimmed)) {
    return null
  }

  const candidate = ABSOLUTE_URL.test(trimmed) ? trimmed : `https://${trimmed}`

  let parsed: URL
  try {
    parsed = new URL(candidate)
  } catch {
    return null
  }

  if (!ALLOWED_PROTOCOLS.has(parsed.protocol)) {
    return null
  }
  if (!parsed.hostname) {
    return null
  }
  return parsed.toString()
}

export const MIN_ZOOM = 0.25
export const MAX_ZOOM = 5

export function clampZoomFactor(factor: unknown): number {
  const value = typeof factor === 'number' ? factor : Number.NaN
  if (!Number.isFinite(value)) {
    return 1
  }
  return Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, value))
}
