import { describe, it, expect } from 'vitest'
import { bytes, compact, count, dayLabel, millis, percent, shortUrl } from './format'

describe('count', () => {
  it('uses English thousands separators', () => {
    expect(count(1234567)).toBe('1,234,567')
  })

  it('returns "—" for invalid values instead of "NaN"', () => {
    expect(count(Number.NaN)).toBe('—')
    expect(count(Number.POSITIVE_INFINITY)).toBe('—')
  })
})

describe('compact', () => {
  it('does not abbreviate below 10,000 — "9,999" is both short and exact', () => {
    expect(compact(9999)).toBe('9,999')
  })

  it('abbreviates thousands and millions', () => {
    expect(compact(12345)).toBe('12.3 K')
    expect(compact(2_500_000)).toBe('2.5 M')
  })
})

describe('percent', () => {
  it('takes a 0..1 ratio, not an already-scaled percentage', () => {
    expect(percent(0.3421)).toBe('34.2%')
    expect(percent(1)).toBe('100.0%')
  })

  it('rounds to the requested number of digits', () => {
    expect(percent(0.3421, 0)).toBe('34%')
  })
})

describe('bytes', () => {
  it('uses multiples of 1024 because these are file sizes', () => {
    expect(bytes(1536)).toBe('1.5 KB')
    expect(bytes(900)).toBe('900 B')
  })

  it('returns "—" for negative numbers', () => {
    expect(bytes(-1)).toBe('—')
  })
})

describe('millis', () => {
  it('does not round a positive value down to "0 ms"', () => {
    expect(millis(0.4)).toBe('< 1 ms')
  })

  it('rounds to whole milliseconds', () => {
    expect(millis(18.6)).toBe('19 ms')
    expect(millis(0)).toBe('0 ms')
  })
})

describe('dayLabel', () => {
  it('drops the year for day-axis labels', () => {
    expect(dayLabel('2026-08-10')).toBe('08/10')
  })

  it('returns the raw string when the format is unexpected', () => {
    expect(dayLabel('today')).toBe('today')
  })
})

describe('shortUrl', () => {
  it('drops the protocol and "www."', () => {
    expect(shortUrl('https://www.vnexpress.net/a')).toBe('vnexpress.net/a')
  })

  it('truncates in the MIDDLE so the distinguishing tail survives', () => {
    const long = `https://vnexpress.net/${'a'.repeat(60)}/important-article`
    const short = shortUrl(long, 40)

    expect(short.length).toBeLessThanOrEqual(40)
    expect(short).toContain('…')
    expect(short).toMatch(/^vnexpress\.net\//)
    expect(short.endsWith('important-article')).toBe(true)
  })

  it('leaves an already short URL untouched', () => {
    expect(shortUrl('https://a.vn/x')).toBe('a.vn/x')
  })
})
