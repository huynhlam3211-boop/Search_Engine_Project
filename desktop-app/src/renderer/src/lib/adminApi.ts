import { API_BASE } from './searchApi'

const REQUEST_TIMEOUT_MS = 8000

export const API_KEY_HEADER = 'X-API-Key'

export type AdminCredential = { kind: 'session'; token: string } | { kind: 'apiKey'; key: string }

function headersFor(credential: AdminCredential): Record<string, string> {
  return credential.kind === 'session'
    ? { Authorization: `Bearer ${credential.token}` }
    : { [API_KEY_HEADER]: credential.key }
}

const TOP_ROWS = 10

export interface Counted {
  label: string
  count: number
}

export interface LinkCount {
  url: string
  host: string
  count: number
  position: number
}

export interface HourPoint {
  hour: string
  visitors: number
  searches: number
  clicks: number
}

export interface LatencyBucket {
  label: string
  count: number
}

export interface TrafficDto {
  visitors: number
  signedInVisitors: number
  activeVisitors: number
  activeWindowMinutes: number
  searches: number
  clicks: number
  clickThroughRate: number
  avgLatencyMs: number
  zeroResultSearches: number
  zeroResultRate: number
  avgSessionMinutes: number
  hourly: HourPoint[]
  latency: LatencyBucket[]
  topQueries: Counted[]
  topLinks: LinkCount[]
  topHosts: Counted[]
  topUsers: Counted[]
  truncated: boolean
}

export interface DayCount {
  date: string
  count: number
}

export interface CrawlDto {
  documents: number
  distinctHosts: number
  totalOutlinks: number
  distinctLinkTargets: number
  avgOutlinks: number
  danglingDocuments: number
  avgDocLength: number
  medianDocLength: number
  oldestCrawledAt: string | null
  newestCrawledAt: string | null
  languages: Counted[]
  topHosts: Counted[]
  crawledPerDay: DayCount[]
}

export interface IndexDto {
  documents: number
  terms: number
  sizeBytes: number
  cacheHitRate: number
  scorer: string
  bloomFilterBits: number
}

export interface AccountStatsDto {
  total: number
  admins: number
  disabled: number
  activeSessions: number
}

export interface DashboardDto {
  generatedAt: string
  traffic: TrafficDto
  crawl: CrawlDto
  index: IndexDto
  accounts: AccountStatsDto
}

export class AdminAuthError extends Error {
  constructor(message = 'The admin key is wrong or has been revoked.') {
    super(message)
    this.name = 'AdminAuthError'
  }
}

export class AdminServerError extends Error {
  constructor(
    readonly status: number,
    message: string
  ) {
    super(message)
    this.name = 'AdminServerError'
  }
}

function explain(status: number, statusText: string): string {
  if (status === 429) {
    return 'The dashboard sent too many requests (limit 120/min). Turn off "Auto refresh" or wait a minute.'
  }
  if (status >= 500) {
    return `The server hit an internal error (${status}). Check the backend log for details.`
  }
  return `The server rejected the request (${status} ${statusText}).`
}

export async function fetchDashboard(
  credential: AdminCredential,
  signal?: AbortSignal
): Promise<DashboardDto> {
  const url = new URL('/api/admin/analytics', API_BASE)
  url.searchParams.set('top', String(TOP_ROWS))

  const response = await fetch(url, {
    headers: { Accept: 'application/json', ...headersFor(credential) },
    signal: signal ?? AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  })

  if (response.status === 401 || response.status === 403) {
    throw new AdminAuthError()
  }
  if (!response.ok) {
    throw new AdminServerError(response.status, explain(response.status, response.statusText))
  }
  return (await response.json()) as DashboardDto
}

export async function resetTraffic(credential: AdminCredential): Promise<void> {
  const response = await fetch(new URL('/api/admin/analytics/reset', API_BASE), {
    method: 'POST',
    headers: headersFor(credential),
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  })
  if (response.status === 401 || response.status === 403) {
    throw new AdminAuthError()
  }
  if (!response.ok) {
    throw new AdminServerError(response.status, explain(response.status, response.statusText))
  }
}

export interface ManagedAccount {
  username: string
  role: 'USER' | 'ADMIN'
  enabled: boolean
  createdAt: string
  lastLoginAt: string | null
}

export async function fetchAccounts(credential: AdminCredential): Promise<ManagedAccount[]> {
  const response = await fetch(new URL('/api/admin/users', API_BASE), {
    headers: { Accept: 'application/json', ...headersFor(credential) },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
  })
  if (response.status === 401 || response.status === 403) {
    throw new AdminAuthError()
  }
  if (!response.ok) {
    throw new AdminServerError(response.status, explain(response.status, response.statusText))
  }
  return (await response.json()) as ManagedAccount[]
}

export async function changeRole(
  credential: AdminCredential,
  username: string,
  role: 'USER' | 'ADMIN'
): Promise<ManagedAccount> {
  const response = await fetch(
    new URL(`/api/admin/users/${encodeURIComponent(username)}/role`, API_BASE),
    {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...headersFor(credential) },
      body: JSON.stringify({ role }),
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
    }
  )
  if (response.status === 401 || response.status === 403) {
    throw new AdminAuthError()
  }
  if (response.status === 400) {
    throw new Error('You cannot demote the account you are currently signed in as.')
  }
  if (!response.ok) {
    throw new AdminServerError(response.status, explain(response.status, response.statusText))
  }
  return (await response.json()) as ManagedAccount
}

export async function deleteAccount(credential: AdminCredential, username: string): Promise<void> {
  const response = await fetch(
    new URL(`/api/admin/users/${encodeURIComponent(username)}`, API_BASE),
    {
      method: 'DELETE',
      headers: headersFor(credential),
      signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS)
    }
  )
  if (response.status === 401 || response.status === 403) {
    throw new AdminAuthError()
  }
  if (response.status === 400) {
    throw new Error('You cannot delete the account you are currently signed in as.')
  }
  if (response.status === 404) {
    throw new Error('This account no longer exists.')
  }
  if (!response.ok) {
    throw new AdminServerError(response.status, explain(response.status, response.statusText))
  }
}
