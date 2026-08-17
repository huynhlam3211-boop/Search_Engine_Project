import { describe, expect, it } from 'vitest'
import { hostOf, prettyUrl, siteGradient, siteInitial } from './site'

describe('hostOf', () => {
  it('extracts the host and strips the www. prefix', () => {
    expect(hostOf('https://www.vnexpress.net/thoi-su')).toBe('vnexpress.net')
    expect(hostOf('https://tuoitre.vn')).toBe('tuoitre.vn')
  })

  it('returns the original string when it cannot be parsed, and does NOT throw', () => {
    expect(hostOf('not-a-url')).toBe('not-a-url')
    expect(hostOf('')).toBe('')
  })

  it('strips www. only at the START, not in the middle', () => {
    expect(hostOf('https://www.vnexpress.net')).toBe('vnexpress.net')
    expect(hostOf('https://mywww.vi-du.com')).toBe('mywww.vi-du.com')
  })
})

describe('prettyUrl', () => {
  it('joins the host with at most three path segments', () => {
    expect(prettyUrl('https://www.vnexpress.net/thoi-su/chinh-tri')).toBe(
      'vnexpress.net › thoi-su › chinh-tri'
    )
  })

  it('truncates when the path has more than three segments', () => {
    expect(prettyUrl('https://vi-du.com/a/b/c/d/e')).toBe('vi-du.com › a › b › c')
  })

  it('leaves only the host when there is no path', () => {
    expect(prettyUrl('https://vi-du.com/')).toBe('vi-du.com')
  })

  it('returns the original string when it cannot be parsed', () => {
    expect(prettyUrl('garbage')).toBe('garbage')
  })
})

describe('siteGradient / siteInitial', () => {
  it('always gives the same color for the same host (stable across renders)', () => {
    const a = siteGradient('https://vnexpress.net/mot-bai')
    const b = siteGradient('https://www.vnexpress.net/mot-bai-khac')
    expect(a).toBe(b)
  })

  it('gives different colors for different hosts', () => {
    expect(siteGradient('https://vnexpress.net')).not.toBe(siteGradient('https://tuoitre.vn'))
  })

  it('produces a valid CSS gradient string', () => {
    expect(siteGradient('https://vi-du.com')).toMatch(/^linear-gradient\(135deg, hsl\(\d+ /)
  })

  it('takes the first letter and uppercases it', () => {
    expect(siteInitial('https://vnexpress.net')).toBe('V')
    expect(siteInitial('https://tuoitre.vn')).toBe('T')
  })

  it('returns "?" when no letter can be extracted', () => {
    expect(siteInitial('')).toBe('?')
    expect(siteInitial('...')).toBe('?')
  })

  it('takes the first letter of the punycode form for internationalized domains', () => {
    expect(siteInitial('https://例え.jp')).toBe('X')
  })
})
