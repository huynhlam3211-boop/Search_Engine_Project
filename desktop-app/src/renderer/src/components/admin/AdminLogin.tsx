import { useState, type FormEvent, type JSX } from 'react'
import { useAdminStore } from '../../store/adminStore'
import { useSessionStore } from '../../store/sessionStore'
import { KeyIcon, ShieldIcon, SpinnerIcon } from '../icons'

function AdminLogin(): JSX.Element {
  const user = useSessionStore((state) => state.user)

  return (
    <div className="flex h-full items-center justify-center overflow-y-auto px-6 py-8">
      <div className="w-full max-w-md">
        <div className="flex flex-col items-center text-center">
          <span className="flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-soft text-brand">
            <ShieldIcon className="h-7 w-7" />
          </span>
          <h2 className="mt-4 text-[19px] font-semibold text-ink">Admin area</h2>
          {user ? (
            <p className="mt-1.5 max-w-sm text-[13px] leading-relaxed text-muted">
              You are signed in as <span className="font-medium text-ink">{user.username}</span>{' '}
              with the <span className="font-medium text-ink">User</span> role. This dashboard
              belongs to the <span className="font-medium text-ink">Administrator</span> role — ask
              an administrator to promote your account.
            </p>
          ) : (
            <p className="mt-1.5 max-w-sm text-[13px] leading-relaxed text-muted">
              Sign in with an account that has the{' '}
              <span className="font-medium text-ink">Administrator</span> role to see the figures.
            </p>
          )}
        </div>

        {!user && <AccountForm />}

        <ApiKeyForm expanded={!!user} />
      </div>
    </div>
  )
}

const INPUT_CLASS =
  'h-10 w-full rounded-xl border border-line bg-omni px-3 text-[13px] text-ink ' +
  'placeholder:text-faint transition focus:border-brand/50 focus:outline-none ' +
  'focus:ring-2 focus:ring-brand/15'

function AccountForm(): JSX.Element {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const signIn = useSessionStore((state) => state.signIn)
  const busy = useSessionStore((state) => state.busy)
  const error = useSessionStore((state) => state.error)

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault()
    if (await signIn(username, password)) {
      setPassword('')
    }
  }

  return (
    <form onSubmit={submit} className="mt-6">
      <label htmlFor="admin-username" className="mb-1.5 block text-[12px] font-medium text-muted">
        Admin account
      </label>
      <div className="space-y-2">
        <input
          id="admin-username"
          value={username}
          onChange={(event) => setUsername(event.target.value)}
          autoFocus
          spellCheck={false}
          autoComplete="username"
          placeholder="Username"
          className={INPUT_CLASS}
        />
        <input
          type="password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          autoComplete="current-password"
          placeholder="Password"
          className={INPUT_CLASS}
          aria-label="Password"
        />
      </div>

      <button
        type="submit"
        disabled={busy || !username.trim() || !password}
        className="mt-3 flex h-10 w-full items-center justify-center rounded-xl text-[13px]
                   font-medium transition enabled:bg-brand enabled:text-white
                   enabled:hover:brightness-110 disabled:cursor-not-allowed disabled:bg-raised
                   disabled:text-faint focus-visible:outline-none focus-visible:ring-2
                   focus-visible:ring-brand/60"
      >
        {busy ? <SpinnerIcon className="h-4 w-4" /> : 'Sign in'}
      </button>

      {error && (
        <p role="alert" className="mt-2.5 text-[12.5px] leading-relaxed text-danger">
          {error}
        </p>
      )}
    </form>
  )
}

function ApiKeyForm({ expanded }: { expanded: boolean }): JSX.Element {
  const [open, setOpen] = useState(expanded)
  const [key, setKey] = useState('')
  const signInWithKey = useAdminStore((state) => state.signInWithKey)
  const verifying = useAdminStore((state) => state.verifying)
  const error = useAdminStore((state) => state.error)

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault()
    if (await signInWithKey(key)) {
      setKey('')
    }
  }

  if (!open) {
    return (
      <button
        onClick={() => setOpen(true)}
        className="mt-4 w-full text-center text-[12px] text-muted underline-offset-2
                   hover:text-ink hover:underline"
      >
        Or use the admin key (meant for operations tooling)
      </button>
    )
  }

  return (
    <form onSubmit={submit} className="mt-6 rounded-2xl border border-line bg-raised/60 p-4">
      <label htmlFor="admin-key" className="mb-1.5 block text-[12px] font-medium text-muted">
        Admin key (the server&apos;s <code className="text-brand">ADMIN_API_KEY</code>)
      </label>
      <div className="flex items-center gap-2">
        <div className="relative min-w-0 flex-1">
          <KeyIcon className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-faint" />
          
          <input
            id="admin-key"
            type="password"
            value={key}
            onChange={(event) => setKey(event.target.value)}
            spellCheck={false}
            autoComplete="off"
            placeholder="Paste the key here"
            className={INPUT_CLASS + ' pl-9'}
          />
        </div>
        <button
          type="submit"
          disabled={verifying || !key.trim()}
          className="h-10 shrink-0 rounded-xl px-4 text-[13px] font-medium transition
                     enabled:bg-brand enabled:text-white enabled:hover:brightness-110
                     disabled:cursor-not-allowed disabled:bg-surface disabled:text-faint
                     focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/60"
        >
          {verifying ? <SpinnerIcon className="h-4 w-4" /> : 'Verify'}
        </button>
      </div>

      {error && (
        <p role="alert" className="mt-2.5 text-[12.5px] leading-relaxed text-danger">
          {error}
        </p>
      )}

      <p className="mt-3 text-[11.5px] leading-relaxed text-faint">
        This key never expires and cannot be revoked, so it is{' '}
        <b>kept in this session&apos;s memory only</b> — closing the app means entering it again.
        An account sign-in token is the opposite: it expires after 12 hours and can be revoked, so
        it is stored and you do not have to sign in every time you open the app.
      </p>
    </form>
  )
}

export default AdminLogin
