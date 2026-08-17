import { useState, type FormEvent, type JSX } from 'react'
import { useSessionStore } from '../store/sessionStore'
import { useAdminStore } from '../store/adminStore'
import { dateTime } from '../lib/format'
import { DeviceIcon, ExitIcon, KeyIcon, ShieldCheckIcon, SpinnerIcon } from './icons'

function AccountMenu({ onNavigateAdmin }: { onNavigateAdmin: () => void }): JSX.Element {
  const user = useSessionStore((state) => state.user)
  return user ? <SignedIn onNavigateAdmin={onNavigateAdmin} /> : <SignedOut />
}

function SignedIn({ onNavigateAdmin }: { onNavigateAdmin: () => void }): JSX.Element {
  const user = useSessionStore((state) => state.user)!
  const signOut = useSessionStore((state) => state.signOut)
  const signOutEverywhere = useSessionStore((state) => state.signOutEverywhere)
  const openScreen = useSessionStore((state) => state.openScreen)
  const notice = useSessionStore((state) => state.notice)
  const busy = useSessionStore((state) => state.busy)
  const clearKey = useAdminStore((state) => state.clearKey)
  const isAdmin = user.role === 'ADMIN'

  return (
    <div className="px-2.5 py-2">
      <div className="flex items-center gap-2.5">
        <span
          className={
            'flex h-9 w-9 shrink-0 items-center justify-center rounded-full text-[13px] font-bold text-white ' +
            (isAdmin
              ? 'bg-linear-to-br from-indigo-500 to-violet-500'
              : 'bg-linear-to-br from-sky-500 to-teal-400')
          }
        >
          {user.username.slice(0, 2).toUpperCase()}
        </span>
        <div className="min-w-0 flex-1">
          <p className="truncate text-[13px] font-medium text-ink">{user.username}</p>
          <p className="mt-0.5 flex items-center gap-1 text-[11.5px]">
            {isAdmin ? (
              <>
                <ShieldCheckIcon className="h-3.5 w-3.5 text-success" />
                <span className="font-medium text-success">Administrator</span>
              </>
            ) : (
              <span className="text-muted">User</span>
            )}
          </p>
        </div>
      </div>

      <dl className="mt-3 space-y-1 border-t border-line pt-2.5 text-[11.5px]">
        <div className="flex justify-between gap-2">
          <dt className="text-faint">Created</dt>
          <dd className="text-muted">{dateTime(user.createdAt)}</dd>
        </div>
        <div className="flex justify-between gap-2">
          <dt className="text-faint">Last sign-in</dt>
          <dd className="text-muted">{dateTime(user.lastLoginAt)}</dd>
        </div>
      </dl>

      {notice && (
        <p className="mt-2.5 rounded-lg bg-success/10 px-2.5 py-2 text-[11.5px] leading-relaxed text-success">
          {notice}
        </p>
      )}

      <div className="menu-sep" />

      {isAdmin && (
        <button onClick={onNavigateAdmin} className="menu-row">
          <ShieldCheckIcon className="h-4 w-4 text-success" />
          Admin dashboard
        </button>
      )}

      <button onClick={() => openScreen('password')} className="menu-row">
        <KeyIcon className="h-4 w-4 text-muted" />
        Change password
      </button>

      <button
        onClick={() => {
          clearKey()
          void signOut()
        }}
        className="menu-row text-danger hover:bg-danger/10"
      >
        <ExitIcon className="h-4 w-4" />
        Sign out
      </button>

      <div className="menu-sep" />
      <button
        onClick={() => {
          clearKey()
          void signOutEverywhere()
        }}
        disabled={busy}
        className="menu-row text-danger hover:bg-danger/10"
        title="Close EVERY session of this account on every device, including this one. Use it when you suspect a session was leaked elsewhere."
      >
        <DeviceIcon className="h-4 w-4" />
        Sign out of all devices
      </button>
    </div>
  )
}

function SignedOut(): JSX.Element {
  const [mode, setMode] = useState<'login' | 'register'>('login')
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')

  const busy = useSessionStore((state) => state.busy)
  const error = useSessionStore((state) => state.error)
  const signIn = useSessionStore((state) => state.signIn)
  const signUp = useSessionStore((state) => state.signUp)
  const openScreen = useSessionStore((state) => state.openScreen)
  const clearError = useSessionStore((state) => state.clearError)

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault()
    const ok =
      mode === 'login' ? await signIn(username, password) : await signUp(username, password)
    if (ok) {
      setPassword('')
      setUsername('')
    }
  }

  const inputClass =
    'h-9 w-full rounded-lg border border-line bg-omni px-2.5 text-[13px] text-ink ' +
    'placeholder:text-faint transition focus:border-brand/50 focus:outline-none ' +
    'focus:ring-2 focus:ring-brand/15'

  return (
    <form onSubmit={submit} className="px-2.5 py-2">
      <p className="text-[13px] font-medium text-ink">
        {mode === 'login' ? 'Sign in' : 'Create account'}
      </p>
      <p className="mt-0.5 text-[11.5px] leading-snug text-faint">
        {mode === 'login'
          ? 'Not signed in — search still works, you just cannot see the admin figures.'
          : 'A new account always gets the User role. Only an administrator can promote it.'}
      </p>

      <div className="mt-2.5 space-y-1.5">
        <input
          value={username}
          onChange={(event) => {
            setUsername(event.target.value)
            clearError()
          }}
          className={inputClass}
          placeholder="Username"
          autoComplete="username"
          spellCheck={false}
          aria-label="Username"
        />
        <input
          type="password"
          value={password}
          onChange={(event) => {
            setPassword(event.target.value)
            clearError()
          }}
          className={inputClass}
          placeholder="Password"
          autoComplete={mode === 'login' ? 'current-password' : 'new-password'}
          aria-label="Password"
        />
      </div>

      {error && (
        <p role="alert" className="mt-2 text-[11.5px] leading-relaxed text-danger">
          {error}
        </p>
      )}

      <button
        type="submit"
        disabled={busy || !username.trim() || !password}
        className="mt-2.5 flex h-9 w-full items-center justify-center rounded-lg text-[13px]
                   font-medium transition enabled:bg-brand enabled:text-white
                   enabled:hover:brightness-110 disabled:cursor-not-allowed
                   disabled:bg-raised disabled:text-faint focus-visible:outline-none
                   focus-visible:ring-2 focus-visible:ring-brand/60"
      >
        {busy ? <SpinnerIcon className="h-4 w-4" /> : mode === 'login' ? 'Sign in' : 'Sign up'}
      </button>

      <button
        type="button"
        onClick={() => {
          setMode(mode === 'login' ? 'register' : 'login')
          clearError()
        }}
        className="mt-2 w-full text-[11.5px] text-muted underline-offset-2 hover:text-ink hover:underline"
      >
        {mode === 'login' ? 'No account yet? Sign up' : 'Already have an account? Sign in'}
      </button>

      <button
        type="button"
        onClick={() => openScreen(mode === 'login' ? 'signin' : 'signup')}
        className="mt-1.5 w-full text-[11.5px] text-brand underline-offset-2 hover:underline"
      >
        Open the full screen
      </button>
    </form>
  )
}

export default AccountMenu
