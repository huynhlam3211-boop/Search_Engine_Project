import { useEffect, useState, type FormEvent, type JSX } from 'react'
import { useSessionStore } from '../../store/sessionStore'
import { useOverlayStore } from '../../store/overlayStore'
import {
  passwordStrength,
  validateConfirmation,
  validatePassword,
  validateUsername
} from '../../lib/validation'
import PasswordField from './PasswordField'
import { CloseIcon, ShieldCheckIcon, SpinnerIcon, UserIcon, VnSearchMark } from '../icons'

function AuthScreen(): JSX.Element | null {
  const screen = useSessionStore((state) => state.screen)
  const close = useSessionStore((state) => state.closeScreen)
  const acquire = useOverlayStore((state) => state.acquire)
  const release = useOverlayStore((state) => state.release)

  useEffect(() => {
    if (!screen) {
      return undefined
    }
    acquire()
    const onKeyDown = (event: KeyboardEvent): void => {
      if (event.key === 'Escape') {
        close()
      }
    }
    window.addEventListener('keydown', onKeyDown)
    return () => {
      window.removeEventListener('keydown', onKeyDown)
      release()
    }
  }, [screen, acquire, release, close])

  if (!screen) {
    return null
  }

  return (
    <div
      className="fixed inset-0 z-50 flex flex-col overflow-y-auto bg-chrome"
      role="dialog"
      aria-modal="true"
      aria-label="VnSearch account"
    >
      <header className="flex h-12 shrink-0 items-center gap-2 px-4">
        <VnSearchMark className="h-5 w-5" />
        <span className="text-[13px] font-semibold text-ink">VnSearch</span>
        <button onClick={close} className="icon-btn ml-auto" aria-label="Close" title="Close (Esc)">
          <CloseIcon className="h-4 w-4" strokeWidth={2.2} />
        </button>
      </header>

      <div className="flex flex-1 items-start justify-center px-6 pb-12 pt-4">
        <div className="w-full max-w-[380px]">
          {screen === 'password' ? <ChangePasswordForm /> : <SignInOrUpForm mode={screen} />}
        </div>
      </div>
    </div>
  )
}

const INPUT_CLASS =
  'h-10 w-full rounded-xl border bg-omni px-3 text-[13px] text-ink placeholder:text-faint ' +
  'transition focus:outline-none focus:ring-2'

function fieldClass(hasError: boolean): string {
  return (
    INPUT_CLASS +
    (hasError
      ? ' border-danger/60 focus:border-danger focus:ring-danger/15'
      : ' border-line focus:border-brand/50 focus:ring-brand/15')
  )
}

function SignInOrUpForm({ mode }: { mode: 'signin' | 'signup' }): JSX.Element {
  const signIn = useSessionStore((state) => state.signIn)
  const signUp = useSessionStore((state) => state.signUp)
  const openScreen = useSessionStore((state) => state.openScreen)
  const busy = useSessionStore((state) => state.busy)
  const serverError = useSessionStore((state) => state.error)

  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [touched, setTouched] = useState<Record<string, boolean>>({})
  const [submitted, setSubmitted] = useState(false)

  const isSignUp = mode === 'signup'
  const usernameError = validateUsername(username)
  const passwordError = isSignUp
    ? validatePassword(password)
    : password
      ? null
      : 'Please enter a password.'
  const confirmationError = isSignUp ? validateConfirmation(password, confirmation) : null
  const strength = isSignUp && password ? passwordStrength(password) : null

  const show = (field: string): boolean => submitted || touched[field] === true
  const canSubmit =
    !busy &&
    !usernameError &&
    !passwordError &&
    (!isSignUp || (!!confirmation && !confirmationError))

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault()
    setSubmitted(true)
    if (!canSubmit) {
      return
    }
    const ok = isSignUp ? await signUp(username, password) : await signIn(username, password)
    if (!ok) {
      setPassword('')
      setConfirmation('')
    }
  }

  return (
    <form onSubmit={submit} noValidate>
      <div className="mb-6 text-center">
        <span className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-soft text-brand">
          <UserIcon className="h-7 w-7" />
        </span>
        <h1 className="mt-4 text-[20px] font-semibold text-ink">
          {isSignUp ? 'Create a VnSearch account' : 'Sign in to VnSearch'}
        </h1>
        <p className="mt-1.5 text-[12.5px] leading-relaxed text-muted">
          {isSignUp
            ? 'A new account always gets the User role. Only an administrator can promote it.'
            : 'Search needs no sign-in — signing in only unlocks what requires knowing who you are.'}
        </p>
      </div>

      <div className="space-y-3.5">
        <div>
          <label
            htmlFor="auth-username"
            className="mb-1.5 block text-[12px] font-medium text-muted"
          >
            Username
          </label>
          <input
            id="auth-username"
            value={username}
            onChange={(event) => setUsername(event.target.value)}
            onBlur={() => setTouched((t) => ({ ...t, username: true }))}
            autoFocus
            spellCheck={false}
            autoComplete="username"
            placeholder="example.username"
            aria-invalid={show('username') && !!usernameError}
            className={fieldClass(show('username') && !!usernameError)}
          />
          {show('username') && usernameError && (
            <p role="alert" className="mt-1.5 text-[11.5px] text-danger">
              {usernameError}
            </p>
          )}
        </div>

        <div onBlur={() => setTouched((t) => ({ ...t, password: true }))}>
          <PasswordField
            id="auth-password"
            label="Password"
            value={password}
            onChange={setPassword}
            autoComplete={isSignUp ? 'new-password' : 'current-password'}
            placeholder={isSignUp ? 'At least 8 characters' : ''}
            error={show('password') ? passwordError : null}
            hint={
              isSignUp && !password
                ? 'A few words strung together is easier to remember and harder to crack than one word with odd characters.'
                : undefined
            }
          />
          {strength && <StrengthMeter strength={strength} />}
        </div>

        {isSignUp && (
          <div onBlur={() => setTouched((t) => ({ ...t, confirmation: true }))}>
            <PasswordField
              id="auth-confirmation"
              label="Confirm password"
              value={confirmation}
              onChange={setConfirmation}
              autoComplete="new-password"
              error={show('confirmation') ? confirmationError : null}
            />
          </div>
        )}
      </div>

      {serverError && (
        <div
          role="alert"
          className="mt-4 rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5
                     text-[12.5px] leading-relaxed text-danger"
        >
          {serverError}
        </div>
      )}

      <button
        type="submit"
        disabled={busy}
        className="mt-5 flex h-10 w-full items-center justify-center rounded-xl text-[13px]
                   font-medium transition enabled:bg-brand enabled:text-white
                   enabled:hover:brightness-110 disabled:cursor-not-allowed disabled:bg-raised
                   disabled:text-faint focus-visible:outline-none focus-visible:ring-2
                   focus-visible:ring-brand/60"
      >
        {busy ? <SpinnerIcon className="h-4 w-4" /> : isSignUp ? 'Create account' : 'Sign in'}
      </button>

      <p className="mt-4 text-center text-[12px] text-muted">
        {isSignUp ? 'Already have an account?' : 'No account yet?'}{' '}
        <button
          type="button"
          onClick={() => openScreen(isSignUp ? 'signin' : 'signup')}
          className="font-medium text-brand underline-offset-2 hover:underline
                     focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
        >
          {isSignUp ? 'Sign in' : 'Sign up'}
        </button>
      </p>
    </form>
  )
}

function StrengthMeter({
  strength
}: {
  strength: { score: 0 | 1 | 2 | 3; label: string; hint: string }
}): JSX.Element {
  const colors = ['bg-danger', 'bg-warn', 'bg-warn', 'bg-success']
  return (
    <div className="mt-2">
      <div className="flex items-center gap-2">
        <div className="flex h-1 flex-1 gap-1" aria-hidden="true">
          {[0, 1, 2].map((segment) => (
            <div
              key={segment}
              className={
                'h-full flex-1 rounded-full ' +
                (segment < strength.score ? colors[strength.score] : 'bg-raised')
              }
            />
          ))}
        </div>
        <span className="w-16 text-right text-[11px] text-muted">{strength.label}</span>
      </div>
      <p className="mt-1 text-[11px] leading-snug text-faint">{strength.hint}</p>
    </div>
  )
}

function ChangePasswordForm(): JSX.Element {
  const user = useSessionStore((state) => state.user)
  const change = useSessionStore((state) => state.changePassword)
  const busy = useSessionStore((state) => state.busy)
  const serverError = useSessionStore((state) => state.error)

  const [current, setCurrent] = useState('')
  const [next, setNext] = useState('')
  const [confirmation, setConfirmation] = useState('')
  const [submitted, setSubmitted] = useState(false)

  const nextError = validatePassword(next)
  const sameAsOld =
    next && current && next === current
      ? 'The new password must differ from the current one.'
      : null
  const confirmationError = validateConfirmation(next, confirmation)
  const strength = next ? passwordStrength(next) : null
  const canSubmit =
    !busy && !!current && !nextError && !sameAsOld && !!confirmation && !confirmationError

  async function submit(event: FormEvent): Promise<void> {
    event.preventDefault()
    setSubmitted(true)
    if (!canSubmit) {
      return
    }
    if (!(await change(current, next))) {
      setCurrent('')
    }
  }

  return (
    <form onSubmit={submit} noValidate>
      <div className="mb-6 text-center">
        <span className="inline-flex h-14 w-14 items-center justify-center rounded-2xl bg-brand-soft text-brand">
          <ShieldCheckIcon className="h-7 w-7" />
        </span>
        <h1 className="mt-4 text-[20px] font-semibold text-ink">Change password</h1>
        <p className="mt-1.5 text-[12.5px] leading-relaxed text-muted">
          Account <span className="font-medium text-ink">{user?.username}</span>. Every{' '}
          <b>other</b> sign-in session will be closed; this device will not.
        </p>
      </div>

      <div className="space-y-3.5">
        <PasswordField
          id="password-current"
          label="Current password"
          value={current}
          onChange={setCurrent}
          autoComplete="current-password"
          autoFocus
          hint="Required again even though you are signed in — details below."
        />
        <div>
          <PasswordField
            id="password-new"
            label="New password"
            value={next}
            onChange={setNext}
            autoComplete="new-password"
            placeholder="At least 8 characters"
            error={submitted ? (nextError ?? sameAsOld) : null}
          />
          {strength && <StrengthMeter strength={strength} />}
        </div>
        <PasswordField
          id="password-confirm"
          label="Confirm new password"
          value={confirmation}
          onChange={setConfirmation}
          autoComplete="new-password"
          error={confirmationError}
        />
      </div>

      {serverError && (
        <div
          role="alert"
          className="mt-4 rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5
                     text-[12.5px] leading-relaxed text-danger"
        >
          {serverError}
        </div>
      )}

      <button
        type="submit"
        disabled={busy}
        className="mt-5 flex h-10 w-full items-center justify-center rounded-xl text-[13px]
                   font-medium transition enabled:bg-brand enabled:text-white
                   enabled:hover:brightness-110 disabled:cursor-not-allowed disabled:bg-raised
                   disabled:text-faint focus-visible:outline-none focus-visible:ring-2
                   focus-visible:ring-brand/60"
      >
        {busy ? <SpinnerIcon className="h-4 w-4" /> : 'Change password'}
      </button>

      <p className="mt-5 rounded-xl border border-line bg-raised px-3.5 py-3 text-[11.5px] leading-relaxed text-faint">
        <b>Why is the current password still required?</b> Because this is where a stolen token gets
        stopped. Without the check, whoever holds the token could change the password and lock you
        out — turning a briefly leaked session into a permanently lost account.
      </p>
    </form>
  )
}

export default AuthScreen
