import { API_BASE } from './searchApi'
import { authHeader } from './authToken'

const SESSION_STORAGE_KEY = 'vnsearch-session-id'

const MAX_QUERY_CHARS = 200
const MAX_URL_CHARS = 500

export interface SessionStore {
  getItem(key: string): string | null
  setItem(key: string, value: string): void
}

export type UsageEvent =
  | { type: 'visit' }
  | { type: 'search'; query: string; resultCount: number; tookMs: number }
  | { type: 'click'; url: string; position: number }

export function readSessionId(store: SessionStore, newId: () => string): string {
  const existing = store.getItem(SESSION_STORAGE_KEY)
  if (existing) {
    return existing
  }
  const created = newId()
  store.setItem(SESSION_STORAGE_KEY, created)
  return created
}

export function buildEventBody(event: UsageEvent, sessionId: string): Record<string, unknown> {
  switch (event.type) {
    case 'visit':
      return { type: 'visit', sessionId }
    case 'search':
      return {
        type: 'search',
        sessionId,
        query: event.query.slice(0, MAX_QUERY_CHARS),
        resultCount: event.resultCount,
        tookMs: Math.max(0, Math.round(event.tookMs))
      }
    case 'click':
      return {
        type: 'click',
        sessionId,
        url: event.url.slice(0, MAX_URL_CHARS),
        position: event.position
      }
  }
}

function browserSessionId(): string {
  return readSessionId(window.localStorage, () => crypto.randomUUID())
}

export function track(event: UsageEvent): void {
  if (event.type === 'search' && !event.query.trim()) {
    return
  }
  if (event.type === 'click' && !event.url) {
    return
  }

  try {
    fetch(new URL('/api/events', API_BASE), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...authHeader() },
      body: JSON.stringify(buildEventBody(event, browserSessionId())),
      keepalive: true
    }).catch(() => {})
  } catch {}
}
