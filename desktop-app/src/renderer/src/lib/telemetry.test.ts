import { describe, it, expect } from 'vitest'
import { buildEventBody, readSessionId, type SessionStore } from './telemetry'

function fakeStore(
  initial: Record<string, string> = {}
): SessionStore & { data: Record<string, string> } {
  const data = { ...initial }
  return {
    data,
    getItem: (key) => data[key] ?? null,
    setItem: (key, value) => {
      data[key] = value
    }
  }
}

describe('readSessionId', () => {
  it('generates a new id and stores it on the first call', () => {
    const store = fakeStore()

    const id = readSessionId(store, () => 'new-id')

    expect(id).toBe('new-id')
    expect(store.getItem('vnsearch-session-id')).toBe('new-id')
  })

  it('reuses the stored id — otherwise every app launch would look like a new "person"', () => {
    const store = fakeStore({ 'vnsearch-session-id': 'old-id' })
    let generated = 0

    const id = readSessionId(store, () => {
      generated++
      return 'must-not-be-used'
    })

    expect(id).toBe('old-id')
    expect(generated).toBe(0)
  })
})

describe('buildEventBody', () => {
  it('the app-open event carries only the session id', () => {
    expect(buildEventBody({ type: 'visit' }, 'p1')).toEqual({ type: 'visit', sessionId: 'p1' })
  })

  it('the search event carries the query, result count and latency', () => {
    const body = buildEventBody(
      { type: 'search', query: 'hanoi', resultCount: 12, tookMs: 18.6 },
      'p1'
    )

    expect(body).toEqual({
      type: 'search',
      sessionId: 'p1',
      query: 'hanoi',
      resultCount: 12,
      tookMs: 19
    })
  })

  it('never sends a negative latency', () => {
    const body = buildEventBody({ type: 'search', query: 'a', resultCount: 0, tookMs: -5 }, 'p1')
    expect(body.tookMs).toBe(0)
  })

  it('truncates an over-long query on the sending side', () => {
    const body = buildEventBody(
      { type: 'search', query: 'x'.repeat(500), resultCount: 1, tookMs: 1 },
      'p1'
    )
    expect(String(body.query)).toHaveLength(200)
  })

  it('truncates an over-long URL', () => {
    const body = buildEventBody(
      { type: 'click', url: `https://a.vn/${'x'.repeat(900)}`, position: 3 },
      'p1'
    )
    expect(String(body.url)).toHaveLength(500)
  })

  it('the click event keeps the rank untouched — it measures ranking quality', () => {
    expect(buildEventBody({ type: 'click', url: 'https://a.vn/1', position: 21 }, 'p1')).toEqual({
      type: 'click',
      sessionId: 'p1',
      url: 'https://a.vn/1',
      position: 21
    })
  })
})
