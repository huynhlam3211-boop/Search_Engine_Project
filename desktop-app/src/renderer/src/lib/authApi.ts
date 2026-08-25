import { API_BASE } from './searchApi'
import { authHeader } from './authToken'

const REQUEST_TIMEOUT_MS = 10_000

export type RoleName = 'USER' | 'ADMIN'

export interface AccountDto {
  username: string
  role: RoleName
  enabled: boolean
  createdAt: string
  lastLoginAt: string | null
}

export interface LoginResponse {
  token: string
  expiresAt: string
  user: AccountDto
}

export class AuthError extends Error {
  constructor(message: string) {
    super(message)
    this.name = 'AuthError'
  }
}

export class ServerError extends Error {
  constructor(
    readonly status: number,
    message: string
  ) {
    super(message)
    this.name = 'ServerError'
  }
}

function explain(status: number, statusText: string): string {
  if (status === 429) {
    return 'Too many requests in one minute. Wait about a minute and try again.'
  }
  if (status >= 500) {
    return `The server hit an internal error (${status}). Check the backend log for details.`
  }
  return `The server rejected the request (${status} ${statusText}).`
}

async function messageOf(response: Response, fallback: string): Promise<string> {
  try {
    const body = (await response.json()) as { message?: string }
    return body?.message?.trim() || fallback
  } catch {
    return fallback
  }
}

async function postJson<T>(path: string, body: unknown, fallbackError: string): Promise<T> {
  const response = await fetch(new URL(path, API_BASE), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Accept: 'application/json', ...authHeader() },
    body: JSON.stringify(body),
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  })

  if (response.status === 401 || response.status === 400) {
    throw new AuthError(await messageOf(response, fallbackError))
  }
  if (!response.ok) {
    throw new ServerError(response.status, explain(response.status, response.statusText))
  }
  if (response.status === 204) {
    return undefined as T
  }
  return (await response.json()) as T
}

export async function register(username: string, password: string): Promise<AccountDto> {
  return postJson<AccountDto>(
    '/api/auth/register',
    { username, password },
    'Could not create the account.'
  )
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  return postJson<LoginResponse>(
    '/api/auth/login',
    { username, password },
    'Incorrect username or password.'
  )
}

export async function logout(): Promise<void> {
  try {
    await fetch(new URL('/api/auth/logout', API_BASE), {
      method: 'POST',
      headers: { ...authHeader() },
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
    })
  } catch {}
}

export async function changePassword(
  currentPassword: string,
  newPassword: string
): Promise<number> {
  const body = await postJson<{ closedOtherSessions?: number }>(
    '/api/auth/password',
    { currentPassword, newPassword },
    'Could not change the password.'
  )
  return body?.closedOtherSessions ?? 0
}

export async function logoutEverywhere(): Promise<number> {
  const body = await postJson<{ closedSessions?: number }>(
    '/api/auth/logout-all',
    {},
    'Could not sign out of the other devices.'
  )
  return body?.closedSessions ?? 0
}

export async function me(): Promise<AccountDto | null> {
  const response = await fetch(new URL('/api/auth/me', API_BASE), {
    headers: { Accept: 'application/json', ...authHeader() },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  })
  if (response.status === 401 || response.status === 403) {
    return null
  }
  if (!response.ok) {
    throw new ServerError(response.status, explain(response.status, response.statusText))
  }
  const body = (await response.json()) as { via?: string; user?: Partial<AccountDto> }
  if (body.via !== 'session' || !body.user?.username) {
    return null
  }
  return {
    username: body.user.username,
    role: body.user.role === 'ADMIN' ? 'ADMIN' : 'USER',
    enabled: body.user.enabled ?? true,
    createdAt: body.user.createdAt ?? '',
    lastLoginAt: body.user.lastLoginAt ?? null
  }
}
