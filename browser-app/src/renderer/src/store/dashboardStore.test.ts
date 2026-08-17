import { describe, it, expect, beforeEach, vi } from 'vitest'
import { AdminAuthError, type AdminCredential, type ManagedAccount } from '../lib/adminApi'

const api = vi.hoisted(() => ({
  fetchAccounts: vi.fn(),
  changeRole: vi.fn(),
  deleteAccount: vi.fn(),
  fetchDashboard: vi.fn(),
  resetTraffic: vi.fn()
}))
const admin = vi.hoisted(() => ({ revoke: vi.fn() }))

vi.mock('../lib/adminApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../lib/adminApi')>()
  return { ...actual, ...api }
})
vi.mock('./adminStore', () => ({
  useAdminStore: { getState: () => admin }
}))

const { useDashboardStore } = await import('./dashboardStore')

const CRED: AdminCredential = { kind: 'session', token: 'fake-token' }
const USER: ManagedAccount = {
  username: 'regularuser',
  role: 'USER',
  enabled: true,
  createdAt: '2026-08-10T10:00:00Z',
  lastLoginAt: null
}

describe('dashboardStore — accounts', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useDashboardStore.setState({ accounts: null, accountsError: null, pendingAccount: null })
  })

  it('loads the account list', async () => {
    api.fetchAccounts.mockResolvedValue([USER])

    await useDashboardStore.getState().loadAccounts(CRED)

    expect(useDashboardStore.getState().accounts).toEqual([USER])
    expect(useDashboardStore.getState().accountsError).toBeNull()
  })

  it('reports the server message on a 401 while loading the list', async () => {
    api.fetchAccounts.mockRejectedValue(new AdminAuthError('The key is no longer valid.'))

    await useDashboardStore.getState().loadAccounts(CRED)

    expect(useDashboardStore.getState().accountsError).toBe('The key is no longer valid.')
  })

  it('RELOADS the list after changing a role', async () => {
    api.changeRole.mockResolvedValue({ ...USER, role: 'ADMIN' })
    api.fetchAccounts.mockResolvedValue([{ ...USER, role: 'ADMIN' }])

    await useDashboardStore.getState().setAccountRole(CRED, 'regularuser', 'ADMIN')

    expect(api.changeRole).toHaveBeenCalledWith(CRED, 'regularuser', 'ADMIN')
    expect(api.fetchAccounts).toHaveBeenCalled()
    expect(useDashboardStore.getState().accounts?.[0].role).toBe('ADMIN')
  })

  it('keeps the server message when changing a role fails', async () => {
    api.changeRole.mockRejectedValue(
      new Error('You cannot demote the account you are currently signed in as.')
    )

    await useDashboardStore.getState().setAccountRole(CRED, 'me', 'USER')

    expect(useDashboardStore.getState().accountsError).toBe(
      'You cannot demote the account you are currently signed in as.'
    )
  })

  it('RELOADS the list after deleting an account', async () => {
    api.deleteAccount.mockResolvedValue(undefined)
    api.fetchAccounts.mockResolvedValue([])

    await useDashboardStore.getState().removeAccount(CRED, 'regularuser')

    expect(api.deleteAccount).toHaveBeenCalledWith(CRED, 'regularuser')
    expect(useDashboardStore.getState().accounts).toEqual([])
  })

  it('always clears pendingAccount, even on failure', async () => {
    api.deleteAccount.mockRejectedValue(new Error('failed'))

    await useDashboardStore.getState().removeAccount(CRED, 'regularuser')

    expect(useDashboardStore.getState().pendingAccount).toBeNull()
  })

  it('clear() wipes both the figures and the account list', () => {
    useDashboardStore.setState({ accounts: [USER], accountsError: 'x' })

    useDashboardStore.getState().clear()

    expect(useDashboardStore.getState().accounts).toBeNull()
    expect(useDashboardStore.getState().accountsError).toBeNull()
  })
})
