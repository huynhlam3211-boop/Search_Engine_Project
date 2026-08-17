import { afterEach, describe, expect, it, vi } from 'vitest'
import { search, searchImages, suggest } from './searchApi'

function mockFetchJson(payload: unknown, ok = true, status = 200): void {
  vi.stubGlobal(
    'fetch',
    vi.fn(async () =>
      Promise.resolve({
        ok,
        status,
        statusText: ok ? 'OK' : 'Internal Server Error',
        json: async () => Promise.resolve(payload)
      } as Response)
    )
  )
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('search', () => {
  it('builds the correct URL and query parameters', async () => {
    mockFetchJson({ query: 'computer', results: [] })
    await search('computer', 2, 15)

    const calledWith = vi.mocked(fetch).mock.calls[0][0] as URL
    expect(calledWith.pathname).toBe('/api/search')
    expect(calledWith.searchParams.get('q')).toBe('computer')
    expect(calledWith.searchParams.get('page')).toBe('2')
    expect(calledWith.searchParams.get('size')).toBe('15')
  })

  it('fills in default values for every missing field', async () => {
    mockFetchJson({})
    const response = await search('something')

    expect(response.query).toBe('something')
    expect(response.results).toEqual([])
    expect(response.totalResults).toBe(0)
    expect(response.droppedTerms).toEqual([])
    expect(response.timeTakenMs).toBe(0)
  })

  it('falls back to the url as the title when a document has no title', async () => {
    mockFetchJson({
      results: [{ url: 'https://example.com/article' }]
    })
    const response = await search('x')

    expect(response.results[0].title).toBe('https://example.com/article')
    expect(response.results[0].score).toBe(0)
  })

  it('TRUSTS the server pageSize rather than the value just sent', async () => {
    mockFetchJson({ pageSize: 20 })
    const response = await search('x', 1, 5000)

    expect(response.pageSize).toBe(20)
  })

  it('throws when the server returns an error code', async () => {
    mockFetchJson({}, false, 500)
    await expect(search('x')).rejects.toThrow('500')
  })
})

describe('searchImages', () => {
  it('drops entries that have no image address', async () => {
    mockFetchJson({
      results: [
        { imageUrl: 'https://example.com/a.jpg' },
        { imageUrl: '' },
        { pageUrl: 'https://example.com/b' }
      ]
    })
    const response = await searchImages('cat')

    expect(response.results).toHaveLength(1)
    expect(response.results[0].imageUrl).toBe('https://example.com/a.jpg')
  })

  it('keeps the server hasMore instead of inferring it from the item count', async () => {
    mockFetchJson({ results: [], hasMore: true, pagesScanned: 12 })
    const response = await searchImages('cat')

    expect(response.hasMore).toBe(true)
    expect(response.pagesScanned).toBe(12)
  })

  it('defaults width/height to -1 when the page does not declare them', async () => {
    mockFetchJson({ results: [{ imageUrl: 'https://example.com/a.jpg' }] })
    const response = await searchImages('cat')

    expect(response.results[0].width).toBe(-1)
    expect(response.results[0].height).toBe(-1)
  })
})

describe('suggest', () => {
  it('returns empty IMMEDIATELY for a blank query, without any network call', async () => {
    mockFetchJson([])
    expect(await suggest('   ')).toEqual([])
    expect(fetch).not.toHaveBeenCalled()
  })

  it('accepts both response shapes: a bare array and one wrapped in { suggestions }', async () => {
    mockFetchJson(['computer', 'camera'])
    expect(await suggest('c')).toEqual(['computer', 'camera'])

    mockFetchJson({ suggestions: ['monitor'] })
    expect(await suggest('mo')).toEqual(['monitor'])
  })

  it('filters out non-string entries', async () => {
    mockFetchJson(['valid', 42, null, { a: 1 }])
    expect(await suggest('v')).toEqual(['valid'])
  })

  it('SWALLOWS network errors and returns empty', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn(async () => Promise.reject(new Error('network down')))
    )
    expect(await suggest('co')).toEqual([])
  })
})
