import { describe, it, expect, beforeEach, vi } from 'vitest'

interface FakeStorage {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
  removeItem(key: string): void
  data: Record<string, string>
}

function fakeStorage(initial: Record<string, string> = {}): FakeStorage {
  const data = { ...initial }
  return {
    data,
    getItem: (key) => data[key] ?? null,
    setItem: (key, value) => {
      data[key] = value
    },
    removeItem: (key) => {
      delete data[key]
    }
  }
}

async function loadModule(storage: FakeStorage | null): Promise<typeof import('./authToken')> {
  vi.resetModules()
  if (storage) {
    ;(globalThis as { window?: unknown }).window = { localStorage: storage }
  } else {
    delete (globalThis as { window?: unknown }).window
  }
  return import('./authToken')
}

describe('authToken', () => {
  beforeEach(() => {
    delete (globalThis as { window?: unknown }).window
  })

  it('with no token yet the header is empty, not a broken header', async () => {
    const { getAuthToken, authHeader } = await loadModule(fakeStorage())

    expect(getAuthToken()).toBeNull()
    expect(authHeader()).toEqual({})
  })

  it('reads back a token stored by a previous run', async () => {
    const { getAuthToken } = await loadModule(fakeStorage({ 'vnsearch-session-token': 'old-token' }))

    expect(getAuthToken()).toBe('old-token')
  })

  it('writing a token updates both memory and disk', async () => {
    const storage = fakeStorage()
    const { setAuthToken, getAuthToken } = await loadModule(storage)

    setAuthToken('new-token')

    expect(getAuthToken()).toBe('new-token')
    expect(storage.data['vnsearch-session-token']).toBe('new-token')
  })

  it('setting null clears it from disk — leaving no "null" string behind', async () => {
    const storage = fakeStorage({ 'vnsearch-session-token': 'old-token' })
    const { setAuthToken, getAuthToken } = await loadModule(storage)

    setAuthToken(null)

    expect(getAuthToken()).toBeNull()
    expect('vnsearch-session-token' in storage.data).toBe(false)
  })

  it('produces the correct Bearer header', async () => {
    const { setAuthToken, authHeader } = await loadModule(fakeStorage())

    setAuthToken('abc123')

    expect(authHeader()).toEqual({ Authorization: 'Bearer abc123' })
  })

  it('does not throw when localStorage is missing', async () => {
    const { getAuthToken, setAuthToken, authHeader } = await loadModule(null)

    expect(() => setAuthToken('abc')).not.toThrow()
    expect(getAuthToken()).toBe('abc') 
    expect(authHeader()).toEqual({ Authorization: 'Bearer abc' })
  })
})
