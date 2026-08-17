import { describe, expect, it } from 'vitest'
import { HOME_URL, MAX_ZOOM, MIN_ZOOM, clampZoomFactor, resolveNavigable } from './urlPolicy'

describe('resolveNavigable — must REJECT', () => {
  it('blocks file:// (local file read path)', () => {
    expect(resolveNavigable('file:///C:/Users/kelly/.ssh/id_rsa')).toBeNull()
    expect(resolveNavigable('file:///etc/passwd')).toBeNull()
    expect(resolveNavigable('file://may-chu-noi-bo/o-chia-se')).toBeNull()
  })

  it('blocks bare schemes with no slashes', () => {
    expect(resolveNavigable('javascript:alert(1)')).toBeNull()
    expect(resolveNavigable('data:text/html,<script>alert(1)</script>')).toBeNull()
    expect(resolveNavigable('mailto:ai-do@vi-du.com')).toBeNull()
    expect(resolveNavigable('vbscript:msgbox(1)')).toBeNull()
  })

  it('blocks uppercase variants and ones with extra whitespace', () => {
    expect(resolveNavigable('FILE:///C:/Windows/win.ini')).toBeNull()
    expect(resolveNavigable('  JavaScript:alert(1)  ')).toBeNull()
  })

  it('blocks internal Chromium and Electron schemes', () => {
    expect(resolveNavigable('chrome://settings')).toBeNull()
    expect(resolveNavigable('devtools://devtools/bundled/inspector.html')).toBeNull()
    expect(resolveNavigable('blob:https://vi-du.com/abc-123')).toBeNull()
  })

  it('blocks empty strings, whitespace-only strings and non-string values', () => {
    expect(resolveNavigable('')).toBeNull()
    expect(resolveNavigable('   ')).toBeNull()
    expect(resolveNavigable(null)).toBeNull()
    expect(resolveNavigable(undefined)).toBeNull()
  })

  it('blocks http/https URLs that do not resolve to a host', () => {
    expect(resolveNavigable('http://')).toBeNull()
    expect(resolveNavigable('https://')).toBeNull()
    expect(resolveNavigable('http:///chi-co-duong-dan')).toBe('http://chi-co-duong-dan/')
  })
})

describe('resolveNavigable — must ALLOW', () => {
  it('keeps full http/https URLs unchanged', () => {
    expect(resolveNavigable('https://vnexpress.net/thoi-su')).toBe('https://vnexpress.net/thoi-su')
    expect(resolveNavigable('http://vi-du.com/a?b=c#d')).toBe('http://vi-du.com/a?b=c#d')
  })

  it('adds https:// to a bare typed domain name', () => {
    expect(resolveNavigable('vnexpress.net')).toBe('https://vnexpress.net/')
    expect(resolveNavigable('  tuoitre.vn/the-thao  ')).toBe('https://tuoitre.vn/the-thao')
  })

  it('does NOT mistake host:port for a scheme — the most common everyday case', () => {
    expect(resolveNavigable('localhost:8080')).toBe('https://localhost:8080/')
    expect(resolveNavigable('localhost:8080/api/health')).toBe('https://localhost:8080/api/health')
    expect(resolveNavigable('127.0.0.1:5173')).toBe('https://127.0.0.1:5173/')
  })

  it('allows the internal home page through untouched', () => {
    expect(resolveNavigable(HOME_URL)).toBe(HOME_URL)
    expect(resolveNavigable(`  ${HOME_URL}  `)).toBe(HOME_URL)
  })

  it('but does NOT allow any other internal app scheme', () => {
    expect(resolveNavigable('vnsearch://cai-gi-do-khac')).toBeNull()
  })
})

describe('clampZoomFactor', () => {
  it('keeps values that are inside the range', () => {
    expect(clampZoomFactor(1)).toBe(1)
    expect(clampZoomFactor(1.5)).toBe(1.5)
  })

  it('clamps out-of-range values to the two bounds', () => {
    expect(clampZoomFactor(0)).toBe(MIN_ZOOM)
    expect(clampZoomFactor(-3)).toBe(MIN_ZOOM)
    expect(clampZoomFactor(1000)).toBe(MAX_ZOOM)
  })

  it('returns 1 for values that are not finite numbers', () => {
    expect(clampZoomFactor(Number.NaN)).toBe(1)
    expect(clampZoomFactor(Number.POSITIVE_INFINITY)).toBe(1)
    expect(clampZoomFactor('2' as unknown as number)).toBe(1)
  })
})
