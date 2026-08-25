import { describe, it, expect, beforeEach, vi } from 'vitest'
import { AuthError, type AccountDto } from '../lib/authApi'

const api = vi.hoisted(() => ({
  login: vi.fn(),
  logout: vi.fn(),
  me: vi.fn(),
  register: vi.fn()
}))
const token = vi.hoisted(() => ({ setAuthToken: vi.fn() }))

vi.mock('../lib/authApi', async (importOriginal) => {
  const actual = await importOriginal<typeof import('../lib/authApi')>()
  return { ...actual, ...api }
})
vi.mock('../lib/authToken', () => token)

const { useSessionStore } = await import('./sessionStore')

const USER: AccountDto = {
  username: 'regularuser',
  role: 'USER',
  enabled: true,
  createdAt: '2026-08-10T10:00:00Z',
  lastLoginAt: null
}

describe('sessionStore', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    useSessionStore.setState({ user: null, ready: false, busy: false, error: null })
  })

  it('stores the token and the user after a successful sign-in', async () => {
    api.login.mockResolvedValue({
      token: 'new-token',
      expiresAt: '2026-08-10T22:00:00Z',
      user: USER
    })

    const ok = await useSessionStore.getState().signIn('regularuser', 'alongpassword')

    expect(ok).toBe(true)
    expect(token.setAuthToken).toHaveBeenCalledWith('new-token')
    expect(useSessionStore.getState().user).toEqual(USER)
    expect(useSessionStore.getState().error).toBeNull()
  })

  it('clears the token and keeps the server message when sign-in fails', async () => {
    api.login.mockRejectedValue(new AuthError('Incorrect username or password.'))

    const ok = await useSessionStore.getState().signIn('regularuser', 'wrong')

    expect(ok).toBe(false)
    expect(token.setAuthToken).toHaveBeenCalledWith(null)
    expect(useSessionStore.getState().user).toBeNull()
    expect(useSessionStore.getState().error).toBe('Incorrect username or password.')
  })

  it('reports a connection problem, not a wrong password, on a network error', async () => {
    api.login.mockRejectedValue(new TypeError('Failed to fetch'))

    await useSessionStore.getState().signIn('regularuser', 'alongpassword')

    expect(useSessionStore.getState().error).toContain('Cannot reach the server')
  })

  it('signs in right after registering instead of asking for the details again', async () => {
    api.register.mockResolvedValue(USER)
    api.login.mockResolvedValue({
      token: 'new-token',
      expiresAt: '2026-08-10T22:00:00Z',
      user: USER
    })

    const ok = await useSessionStore.getState().signUp('regularuser', 'alongpassword')

    expect(ok).toBe(true)
    expect(api.login).toHaveBeenCalledWith('regularuser', 'alongpassword')
    expect(useSessionStore.getState().user).toEqual(USER)
  })

  it('does NOT attempt a sign-in when registration fails', async () => {
    api.register.mockRejectedValue(new AuthError('Username already exists: regularuser'))

    const ok = await useSessionStore.getState().signUp('regularuser', 'alongpassword')

    expect(ok).toBe(false)
    expect(api.login).not.toHaveBeenCalled()
    expect(useSessionStore.getState().error).toContain('already exists')
  })

  it('restores the session according to the SERVER', async () => {
    api.me.mockResolvedValue(USER)

    await useSessionStore.getState().restore()

    expect(useSessionStore.getState().user).toEqual(USER)
    expect(useSessionStore.getState().ready).toBe(true)
  })

  it('drops the token when the server says it is no longer valid', async () => {
    api.me.mockResolvedValue(null)

    await useSessionStore.getState().restore()

    expect(token.setAuthToken).toHaveBeenCalledWith(null)
    expect(useSessionStore.getState().user).toBeNull()
    expect(useSessionStore.getState().ready).toBe(true)
  })

  it('does NOT clear the token when the connection drops during startup', async () => {
    api.me.mockRejectedValue(new TypeError('Failed to fetch'))

    await useSessionStore.getState().restore()

    expect(token.setAuthToken).not.toHaveBeenCalled()
    expect(useSessionStore.getState().ready).toBe(true)
  })

  it('tells the server on sign-out, then clears the local state', async () => {
    useSessionStore.setState({ user: USER })
    api.logout.mockResolvedValue(undefined)

    await useSessionStore.getState().signOut()

    expect(api.logout).toHaveBeenCalled()
    expect(token.setAuthToken).toHaveBeenCalledWith(null)
    expect(useSessionStore.getState().user).toBeNull()
  })
})
