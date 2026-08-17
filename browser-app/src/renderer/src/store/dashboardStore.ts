import { create } from 'zustand'
import {
  AdminAuthError,
  AdminServerError,
  changeRole,
  deleteAccount,
  fetchAccounts,
  fetchDashboard,
  resetTraffic,
  type AdminCredential,
  type DashboardDto,
  type ManagedAccount
} from '../lib/adminApi'
import { useAdminStore } from './adminStore'

let inFlight: AbortController | null = null

interface DashboardState {
  data: DashboardDto | null
  error: string | null
  loading: boolean

  load: (credential: AdminCredential, spinner?: boolean) => Promise<void>
  resetTraffic: (credential: AdminCredential) => Promise<void>
  accounts: ManagedAccount[] | null
  accountsError: string | null
  pendingAccount: string | null

  loadAccounts: (credential: AdminCredential) => Promise<void>
  setAccountRole: (
    credential: AdminCredential,
    username: string,
    role: 'USER' | 'ADMIN'
  ) => Promise<void>
  removeAccount: (credential: AdminCredential, username: string) => Promise<void>

  clear: () => void
}

export const useDashboardStore = create<DashboardState>((set) => ({
  data: null,
  error: null,
  loading: false,
  accounts: null,
  accountsError: null,
  pendingAccount: null,

  load: async (credential, spinner = true) => {
    inFlight?.abort()
    const controller = new AbortController()
    inFlight = controller
    if (spinner) {
      set({ loading: true })
    }

    try {
      const data = await fetchDashboard(credential, controller.signal)
      set({ data, error: null })
    } catch (caught) {
      if (controller.signal.aborted) {
        return
      }
      if (caught instanceof AdminAuthError) {
        useAdminStore.getState().revoke()
        return
      }
      set({
        error:
          caught instanceof AdminServerError
            ? caught.message
            : 'Cannot reach the server. The figures below are from the last successful load.'
      })
    } finally {
      if (!controller.signal.aborted) {
        set({ loading: false })
      }
    }
  },

  resetTraffic: async (credential) => {
    try {
      await resetTraffic(credential)
    } catch (caught) {
      set({
        error:
          caught instanceof AdminAuthError
            ? caught.message
            : 'Could not reset the traffic figures.'
      })
      return
    }
    await useDashboardStore.getState().load(credential, false)
  },

  loadAccounts: async (credential) => {
    try {
      set({ accounts: await fetchAccounts(credential), accountsError: null })
    } catch (caught) {
      set({
        accountsError:
          caught instanceof AdminAuthError ? caught.message : 'Could not load the account list.'
      })
    }
  },

  setAccountRole: async (credential, username, role) => {
    set({ pendingAccount: username, accountsError: null })
    try {
      await changeRole(credential, username, role)
      set({ accounts: await fetchAccounts(credential) })
    } catch (caught) {
      set({
        accountsError: caught instanceof Error ? caught.message : 'Could not change the role.'
      })
    } finally {
      set({ pendingAccount: null })
    }
  },

  removeAccount: async (credential, username) => {
    set({ pendingAccount: username, accountsError: null })
    try {
      await deleteAccount(credential, username)
      set({ accounts: await fetchAccounts(credential) })
    } catch (caught) {
      set({ accountsError: caught instanceof Error ? caught.message : 'Could not delete the account.' })
    } finally {
      set({ pendingAccount: null })
    }
  },

  clear: () => {
    inFlight?.abort()
    inFlight = null
    set({
      data: null,
      error: null,
      loading: false,
      accounts: null,
      accountsError: null,
      pendingAccount: null
    })
  }
}))
