import { useState, type JSX } from 'react'
import { EyeIcon, EyeOffIcon } from '../icons'

function PasswordField({
  id,
  label,
  value,
  onChange,
  placeholder,
  autoComplete,
  autoFocus,
  error,
  hint
}: {
  id: string
  label: string
  value: string
  onChange: (value: string) => void
  placeholder?: string
  autoComplete: 'current-password' | 'new-password'
  autoFocus?: boolean
  error?: string | null
  hint?: string
}): JSX.Element {
  const [visible, setVisible] = useState(false)

  return (
    <div>
      <label htmlFor={id} className="mb-1.5 block text-[12px] font-medium text-muted">
        {label}
      </label>
      <div className="relative">
        <input
          id={id}
          type={visible ? 'text' : 'password'}
          value={value}
          onChange={(event) => onChange(event.target.value)}
          placeholder={placeholder}
          autoComplete={autoComplete}
          autoFocus={autoFocus}
          spellCheck={false}
          aria-invalid={!!error}
          aria-describedby={error ? `${id}-error` : hint ? `${id}-hint` : undefined}
          className={
            'h-10 w-full rounded-xl border bg-omni pl-3 pr-10 text-[13px] text-ink ' +
            'placeholder:text-faint transition focus:outline-none focus:ring-2 ' +
            (error
              ? 'border-danger/60 focus:border-danger focus:ring-danger/15'
              : 'border-line focus:border-brand/50 focus:ring-brand/15')
          }
        />
        <button
          type="button"
          onClick={() => setVisible((shown) => !shown)}
          className="absolute right-1 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center
                     justify-center rounded-lg text-faint transition hover:bg-raised
                     hover:text-ink focus-visible:outline-none focus-visible:ring-2
                     focus-visible:ring-brand/50"
          aria-label={visible ? 'Hide password' : 'Show password'}
          title={visible ? 'Hide password' : 'Show password'}
        >
          {visible ? (
            <EyeOffIcon className="h-[17px] w-[17px]" />
          ) : (
            <EyeIcon className="h-[17px] w-[17px]" />
          )}
        </button>
      </div>

      {error ? (
        <p id={`${id}-error`} role="alert" className="mt-1.5 text-[11.5px] text-danger">
          {error}
        </p>
      ) : hint ? (
        <p id={`${id}-hint`} className="mt-1.5 text-[11.5px] text-faint">
          {hint}
        </p>
      ) : null}
    </div>
  )
}

export default PasswordField
