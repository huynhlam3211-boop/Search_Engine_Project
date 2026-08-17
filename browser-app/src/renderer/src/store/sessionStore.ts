import { create } from 'zustand'
import {
  AuthError,
  ServerError,
  changePassword,
  login,
  logout,
  logoutEverywhere,
  me,
  register,
  type AccountDto
} from '../lib/authApi'
import { setAuthToken } from '../lib/authToken'

export type AuthScreen = 'signin' | 'signup' | 'password'

interface SessionState {
  user: AccountDto | null
  ready: boolean
  busy: boolean
  error: string | null
  notice: string | null
  screen: AuthScreen | null

  restore: () => Promise<void>
  signIn: (username: string, password: string) => Promise<boolean>
  signUp: (username: string, password: string) => Promise<boolean>
  signOut: () => Promise<void>
  changePassword: (currentPassword: string, newPassword: string) => Promise<boolean>
  signOutEverywhere: () => Promise<void>
  openScreen: (screen: AuthScreen) => void
  closeScreen: () => void
  clearError: () => void
}

function describe(error: unknown): string {
  if (error instanceof AuthError || error instanceof ServerError) {
    return error.message
  }
  return 'Cannot reach the server (http://localhost:8080). Check that the backend is running.'
}

export const useSessionStore = create<SessionState>((set) => ({
  user: null,
  ready: false,
  busy: false,
  error: null,
  notice: null,
  screen: null,

  restore: async () => {
    try {
      const user = await me()
      if (!user) {
        setAuthToken(null)
      }
      set({ user, ready: true })
    } catch {
      set({ user: null, ready: true })
    }
  },

  signIn: async (username, password) => {
    set({ busy: true, error: null })
    try {
      const response = await login(username, password)
      setAuthToken(response.token)
      set({ user: response.user, busy: false, error: null, ready: true, screen: null })
      return true
    } catch (error) {
      setAuthToken(null)
      set({ user: null, busy: false, error: describe(error) })
      return false
    }
  },

  signUp: async (username, password) => {
    set({ busy: true, error: null })
    try {
      await register(username, password)
    } catch (error) {
      set({ busy: false, error: describe(error) })
      return false
    }
    return useSessionStore.getState().signIn(username, password)
  },

  signOut: async () => {
    await logout()
    setAuthToken(null)
    set({ user: null, error: null, notice: null, screen: null })
  },

  changePassword: async (currentPassword, newPassword) => {
    set({ busy: true, error: null, notice: null })
    try {
      const closed = await changePassword(currentPassword, newPassword)
      set({
        busy: false,
        screen: null,
        notice:
          closed > 0
            ? `Password changed. ${closed} other sessions were signed out.`
            : 'Password changed.'
      })
      return true
    } catch (error) {
      set({ busy: false, error: describe(error) })
      return false
    }
  },

  signOutEverywhere: async () => {
    set({ busy: true, error: null })
    try {
      await logoutEverywhere()
    } catch {}
    setAuthToken(null)
    set({ user: null, busy: false, error: null, notice: null, screen: null })
  },

  openScreen: (screen) => set({ screen, error: null, notice: null }),

  closeScreen: () => set({ screen: null, error: null }),

  clearError: () => set({ error: null })
}))
