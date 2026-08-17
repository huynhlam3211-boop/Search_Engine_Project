import { useMemo } from 'react'
import { create } from 'zustand'
import { AdminAuthError, fetchDashboard, type AdminCredential } from '../lib/adminApi'
import { getAuthToken } from '../lib/authToken'
import { useSessionStore } from './sessionStore'

interface AdminState {
  apiKey: string | null
  dashboardOpen: boolean
  verifying: boolean
  error: string | null

  signInWithKey: (key: string) => Promise<boolean>
  clearKey: () => void
  openDashboard: () => void
  closeDashboard: () => void
  revoke: (message?: string) => void
  setError: (message: string | null) => void
}

export const useAdminStore = create<AdminState>((set) => ({
  apiKey: null,
  dashboardOpen: false,
  verifying: false,
  error: null,

  signInWithKey: async (key) => {
    const trimmed = key.trim()
    if (!trimmed) {
      set({ error: 'Please enter the admin key.' })
      return false
    }
    set({ verifying: true, error: null })
    try {
      await fetchDashboard({ kind: 'apiKey', key: trimmed })
      set({ apiKey: trimmed, verifying: false, error: null })
      return true
    } catch (error) {
      set({
        apiKey: null,
        verifying: false,
        error:
          error instanceof AdminAuthError
            ? error.message
            : 'Cannot reach the server (http://localhost:8080). Check that the backend is running.'
      })
      return false
    }
  },

  clearKey: () => set({ apiKey: null, error: null }),

  openDashboard: () => set({ dashboardOpen: true }),

  closeDashboard: () => set({ dashboardOpen: false }),

  revoke: (message) =>
    set({
      apiKey: null,
      error: message ?? 'Admin access is no longer valid. Please authenticate again.'
    }),

  setError: (message) => set({ error: message })
}))

export function useAdminCredential(): AdminCredential | null {
  const role = useSessionStore((state) => state.user?.role)
  const apiKey = useAdminStore((state) => state.apiKey)
  const token = role === 'ADMIN' ? getAuthToken() : null

  return useMemo(() => {
    if (token) {
      return { kind: 'session', token }
    }
    return apiKey ? { kind: 'apiKey', key: apiKey } : null
  }, [token, apiKey])
}
