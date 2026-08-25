const STORAGE_KEY = 'vnsearch-session-token'

let cached: string | null | undefined

function storage(): Storage | null {
  try {
    return window.localStorage
  } catch {
    return null
  }
}

export function getAuthToken(): string | null {
  if (cached === undefined) {
    cached = storage()?.getItem(STORAGE_KEY) ?? null
  }
  return cached
}

export function setAuthToken(token: string | null): void {
  cached = token
  const store = storage()
  if (!store) {
    return
  }
  if (token) {
    store.setItem(STORAGE_KEY, token)
  } else {
    store.removeItem(STORAGE_KEY)
  }
}

export function authHeader(): Record<string, string> {
  const token = getAuthToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}
