import type { JSX } from 'react'
import { useAdminCredential } from '../../store/adminStore'
import { useSessionStore } from '../../store/sessionStore'

interface Rule {
  method: string
  path: string
  guest: boolean
  user: boolean
  admin: boolean
  note: string
}

const RULES: Rule[] = [
  {
    method: 'GET',
    path: '/api/search',
    guest: true,
    user: true,
    admin: true,
    note: 'Search — the core feature, NO sign-in required'
  },
  {
    method: 'GET',
    path: '/api/suggest · /api/images · /api/feed',
    guest: true,
    user: true,
    admin: true,
    note: 'Suggestions, images, feed'
  },
  {
    method: 'GET',
    path: '/api/health',
    guest: true,
    user: true,
    admin: true,
    note: 'Health — Docker must be able to call it with nothing at all'
  },
  {
    method: 'POST',
    path: '/api/events',
    guest: true,
    user: true,
    admin: true,
    note: 'WRITES metrics: close it and there are no metrics left to read'
  },
  {
    method: 'POST',
    path: '/api/auth/register · login',
    guest: true,
    user: true,
    admin: true,
    note: 'The front door — someone without an account has to be able to knock'
  },
  {
    method: 'GET',
    path: '/api/auth/me · logout',
    guest: false,
    user: true,
    admin: true,
    note: 'Only requires being signed in, regardless of role'
  },
  {
    method: 'POST',
    path: '/api/auth/password',
    guest: false,
    user: true,
    admin: true,
    note: 'Change password. The current password is still required'
  },
  {
    method: 'POST',
    path: '/api/auth/logout-all',
    guest: false,
    user: true,
    admin: true,
    note: 'Closes every session, including the one in use'
  },
  {
    method: 'GET',
    path: '/api/admin/analytics',
    guest: false,
    user: false,
    admin: true,
    note: 'READS metrics — exposes what users are searching for'
  },
  {
    method: 'GET',
    path: '/api/admin/users',
    guest: false,
    user: false,
    admin: true,
    note: 'Account list (without password hashes)'
  },
  {
    method: 'POST',
    path: '/api/admin/users/{name}/role',
    guest: false,
    user: false,
    admin: true,
    note: 'Promote/demote a role. You cannot demote yourself'
  },
  {
    method: 'POST',
    path: '/api/admin/users/{name}/disable · enable',
    guest: false,
    user: false,
    admin: true,
    note: 'Disable/enable an account while keeping its data'
  },
  {
    method: 'DELETE',
    path: '/api/admin/users/{name}',
    guest: false,
    user: false,
    admin: true,
    note: 'Deletes for good. Cannot be undone, and you cannot delete yourself'
  },
  {
    method: 'GET',
    path: '/api/admin/stats',
    guest: false,
    user: false,
    admin: true,
    note: 'Operational details of the index'
  },
  {
    method: 'POST',
    path: '/api/admin/crawl',
    guest: false,
    user: false,
    admin: true,
    note: 'Makes the server fetch arbitrary URLs — the riskiest endpoint'
  },
  {
    method: 'POST',
    path: '/api/admin/reindex',
    guest: false,
    user: false,
    admin: true,
    note: 'Rebuilds the index, resource heavy'
  }
]

function Mark({ allowed, deniedCode }: { allowed: boolean; deniedCode: 401 | 403 }): JSX.Element {
  return allowed ? (
    <span className="text-success" title="Allowed">
      ✓
    </span>
  ) : (
    <span className="text-faint" title={`Denied (${deniedCode})`}>
      ✕
    </span>
  )
}

function PermissionMatrix(): JSX.Element {
  const user = useSessionStore((state) => state.user)
  const credential = useAdminCredential()

  const currentColumn = credential ? 'admin' : user ? 'user' : 'guest'
  const columnClass = (column: string): string =>
    column === currentColumn ? 'bg-brand-soft/60' : ''

  return (
    <div className="overflow-x-auto">
      <table className="w-full min-w-[520px] text-left text-[12px]">
        <thead>
          <tr className="border-b border-line text-[11px] uppercase tracking-wide text-faint">
            <th className="py-2 pr-3 font-medium">Endpoint</th>
            <th className={'w-20 py-2 text-center font-medium ' + columnClass('guest')}>Guest</th>
            <th className={'w-24 py-2 text-center font-medium ' + columnClass('user')}>
              User
            </th>
            <th className={'w-20 py-2 text-center font-medium ' + columnClass('admin')}>Admin</th>
            <th className="py-2 pl-3 font-medium">Why</th>
          </tr>
        </thead>
        <tbody>
          {RULES.map((rule) => (
            <tr key={rule.method + rule.path} className="border-b border-line/60">
              <td className="py-1.5 pr-3">
                <span className="mr-1.5 rounded bg-raised px-1.5 py-0.5 font-mono text-[10.5px] text-muted">
                  {rule.method}
                </span>
                <span className="text-ink">{rule.path}</span>
              </td>
              <td className={'py-1.5 text-center ' + columnClass('guest')}>
                <Mark allowed={rule.guest} deniedCode={401} />
              </td>
              <td className={'py-1.5 text-center ' + columnClass('user')}>
                <Mark allowed={rule.user} deniedCode={403} />
              </td>
              <td className={'py-1.5 text-center ' + columnClass('admin')}>
                <Mark allowed={rule.admin} deniedCode={403} />
              </td>
              <td className="py-1.5 pl-3 text-faint">{rule.note}</td>
            </tr>
          ))}
        </tbody>
      </table>

      <p className="mt-3 text-[11.5px] leading-relaxed text-faint">
        The highlighted column is your role right now:{' '}
        <span className="font-medium text-ink">
          {credential
            ? credential.kind === 'session'
              ? `ADMIN — account ${user?.username}`
              : 'ADMIN — X-API-Key admin key, with no account behind it'
            : user
              ? `USER — ${user.username}`
              : 'GUEST — not signed in'}
        </span>
        .
      </p>
      <p className="mt-1.5 text-[11.5px] leading-relaxed text-faint">
        The real rules live in <code>SecurityConfig</code> on the server; this table is only a
        transcript for the reader. Hiding or showing a button in the UI changes nothing — a{' '}
        <code>curl</code> call that carries no proof of permission still gets a 401.
      </p>
    </div>
  )
}

export default PermissionMatrix
