import { useEffect, useState, type JSX } from 'react'
import type { AdminCredential, ManagedAccount } from '../../lib/adminApi'
import { useDashboardStore } from '../../store/dashboardStore'
import { useSessionStore } from '../../store/sessionStore'
import { dateTime } from '../../lib/format'
import { AlertIcon, ShieldCheckIcon, SpinnerIcon, TrashIcon, UserIcon } from '../icons'

function AccountsTable({ credential }: { credential: AdminCredential }): JSX.Element {
  const currentUser = useSessionStore((state) => state.user)
  const accounts = useDashboardStore((state) => state.accounts)
  const error = useDashboardStore((state) => state.accountsError)
  const pending = useDashboardStore((state) => state.pendingAccount)
  const loadAccounts = useDashboardStore((state) => state.loadAccounts)
  const setAccountRole = useDashboardStore((state) => state.setAccountRole)
  const removeAccount = useDashboardStore((state) => state.removeAccount)

  const [confirming, setConfirming] = useState<string | null>(null)

  useEffect(() => {
    loadAccounts(credential)
  }, [credential, loadAccounts])

  if (accounts === null && error === null) {
    return <div className="skeleton h-32 rounded-xl" aria-hidden="true" />
  }

  const admins = (accounts ?? []).filter((account) => account.role === 'ADMIN').length

  return (
    <div>
      {error && (
        <div className="mb-3 flex items-start gap-2.5 rounded-xl border border-danger/25 bg-danger/5 px-3.5 py-2.5">
          <AlertIcon className="mt-0.5 h-4 w-4 shrink-0 text-danger" />
          <p className="text-[12.5px] leading-relaxed text-danger">{error}</p>
        </div>
      )}

      <div className="overflow-x-auto">
        <table className="w-full min-w-[560px] text-left text-[12.5px]">
          <thead>
            <tr className="border-b border-line text-[11px] uppercase tracking-wide text-faint">
              <th className="py-2 pr-3 font-medium">Account</th>
              <th className="w-28 py-2 font-medium">Role</th>
              <th className="w-36 py-2 font-medium">Last sign-in</th>
              <th className="w-64 py-2 text-right font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            {(accounts ?? []).map((account) => (
              <AccountRow
                key={account.username}
                account={account}
                isSelf={currentUser?.username.toLowerCase() === account.username.toLowerCase()}
                lastAdmin={account.role === 'ADMIN' && admins === 1}
                pending={pending === account.username}
                busy={pending !== null}
                confirming={confirming === account.username}
                onSwitch={() =>
                  setAccountRole(
                    credential,
                    account.username,
                    account.role === 'ADMIN' ? 'USER' : 'ADMIN'
                  )
                }
                onAskDelete={() => setConfirming(account.username)}
                onCancelDelete={() => setConfirming(null)}
                onConfirmDelete={() => {
                  setConfirming(null)
                  void removeAccount(credential, account.username)
                }}
              />
            ))}
          </tbody>
        </table>
      </div>

      <p className="mt-3 text-[11.5px] leading-relaxed text-faint">
        Changing a role <b>closes every session</b> of that account — even when promoting it.
        Otherwise the old session keeps the old role, so the change is only on paper while the old
        permissions stay live for hours.
      </p>
    </div>
  )
}

function AccountRow({
  account,
  isSelf,
  lastAdmin,
  pending,
  busy,
  confirming,
  onSwitch,
  onAskDelete,
  onCancelDelete,
  onConfirmDelete
}: {
  account: ManagedAccount
  isSelf: boolean
  lastAdmin: boolean
  pending: boolean
  busy: boolean
  confirming: boolean
  onSwitch: () => void
  onAskDelete: () => void
  onCancelDelete: () => void
  onConfirmDelete: () => void
}): JSX.Element {
  const isAdmin = account.role === 'ADMIN'

  return (
    <tr className="border-b border-line/60">
      <td className="py-2 pr-3">
        <span className="flex items-center gap-2">
          <span
            className={
              'flex h-6 w-6 shrink-0 items-center justify-center rounded-full ' +
              'text-[10px] font-bold text-white ' +
              (isAdmin
                ? 'bg-linear-to-br from-indigo-500 to-violet-500'
                : 'bg-linear-to-br from-sky-500 to-teal-400')
            }
          >
            {account.username.slice(0, 2).toUpperCase()}
          </span>
          <span className="min-w-0">
            <span className="block truncate text-ink">{account.username}</span>
            {isSelf && <span className="text-[11px] text-faint">you</span>}
            {!account.enabled && <span className="text-[11px] text-warn">disabled</span>}
          </span>
        </span>
      </td>
      <td className="py-2">
        <span
          className={
            'inline-flex items-center gap-1 rounded-full px-2 py-0.5 text-[11px] font-medium ' +
            (isAdmin ? 'bg-success/15 text-success' : 'bg-raised text-muted')
          }
        >
          {isAdmin ? (
            <ShieldCheckIcon className="h-3.5 w-3.5" />
          ) : (
            <UserIcon className="h-3.5 w-3.5" />
          )}
          {isAdmin ? 'Admin' : 'User'}
        </span>
      </td>
      <td className="py-2 text-faint">{dateTime(account.lastLoginAt)}</td>
      <td className="py-2 text-right">
        {confirming ? (
          <span className="inline-flex items-center gap-2">
            <span className="text-[11.5px] text-danger">Permanently delete {account.username}?</span>
            <button
              onClick={onConfirmDelete}
              className="h-7 rounded-full bg-danger px-2.5 text-[11.5px] font-medium text-white
                         transition hover:brightness-110 focus-visible:outline-none
                         focus-visible:ring-2 focus-visible:ring-danger/50"
            >
              Delete
            </button>
            <button
              onClick={onCancelDelete}
              className="h-7 rounded-full border border-line px-2.5 text-[11.5px] text-muted
                         transition hover:bg-raised hover:text-ink focus-visible:outline-none"
            >
              Cancel
            </button>
          </span>
        ) : (
          <span className="inline-flex items-center gap-1.5">
            <button
              onClick={onSwitch}
              disabled={isSelf || busy}
              className="inline-flex h-7 items-center gap-1.5 rounded-full border border-line
                     px-2.5 text-[11.5px] text-muted transition
                     enabled:hover:bg-raised enabled:hover:text-ink
                     disabled:cursor-not-allowed disabled:opacity-45
                     focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-brand/50"
              title={
                isSelf
                  ? 'You cannot change your own role — if the last administrator demotes themselves, nobody can promote anyone again.'
                  : lastAdmin
                    ? 'WARNING: this is the only administrator. Demoting them leaves no account-based admin access (the API key entry point still works).'
                    : `Change to ${isAdmin ? 'User' : 'Admin'}. Every session of this account will be closed.`
              }
            >
              {pending ? (
                <SpinnerIcon className="h-3.5 w-3.5" />
              ) : isAdmin ? (
                'Demote to User'
              ) : (
                'Promote to Admin'
              )}
            </button>
            <button
              onClick={onAskDelete}
              disabled={isSelf || busy}
              className="flex h-7 w-7 items-center justify-center rounded-full border border-line
                         text-faint transition enabled:hover:border-danger/40
                         enabled:hover:bg-danger/10 enabled:hover:text-danger
                         disabled:cursor-not-allowed disabled:opacity-40
                         focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-danger/40"
              aria-label={`Delete account ${account.username}`}
              title={
                isSelf
                  ? 'You cannot delete the account you are signed in as.'
                  : 'Delete the account for good. Unlike disabling: it cannot be undone, and the name is freed for someone else to register.'
              }
            >
              <TrashIcon className="h-3.5 w-3.5" />
            </button>
          </span>
        )}
      </td>
    </tr>
  )
}

export default AccountsTable
