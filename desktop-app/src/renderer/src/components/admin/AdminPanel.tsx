import { useEffect, useState, type JSX } from 'react'
import { useAdminCredential, useAdminStore } from '../../store/adminStore'
import { useDashboardStore } from '../../store/dashboardStore'
import { useOverlayStore } from '../../store/overlayStore'
import { useSessionStore } from '../../store/sessionStore'
import type { AdminCredential, DashboardDto } from '../../lib/adminApi'
import { coverageRatio, foldTail, headShare, normalizedEntropy, ratio } from '../../lib/analysis'
import {
  bytes,
  compact,
  count,
  dateTime,
  dayLabel,
  millis,
  percent,
  shortUrl
} from '../../lib/format'
import AdminLogin from './AdminLogin'
import PermissionMatrix from './PermissionMatrix'
import AccountsTable from './AccountsTable'
import { BarList, ChartCard, ColumnChart, ShareBar, StatTile, TrendChart } from './charts'
import {
  AlertIcon,
  ChartIcon,
  CloseIcon,
  DatabaseIcon,
  ExitIcon,
  ReloadIcon,
  ShieldCheckIcon,
  SpinnerIcon,
  TrashIcon,
  UsersIcon
} from '../icons'

const REFRESH_MS = 10_000
function AdminPanel(): JSX.Element | null {
  const open = useAdminStore((state) => state.dashboardOpen)
  const close = useAdminStore((state) => state.closeDashboard)
  const credential = useAdminCredential()
  const acquire = useOverlayStore((state) => state.acquire)
  const release = useOverlayStore((state) => state.release)

  useEffect(() => {
    if (!open) {
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
  }, [open, acquire, release, close])

  if (!open) {
    return null
  }

  return (
    <div
      className="fixed inset-0 z-50 flex flex-col bg-surface"
      role="dialog"
      aria-modal="true"
      aria-label="Admin dashboard"
    >
      {credential ? (
        <Dashboard credential={credential} onClose={close} />
      ) : (
        <GuestView onClose={close} />
      )}
    </div>
  )
}

function GuestView({ onClose }: { onClose: () => void }): JSX.Element {
  return (
    <>
      <header className="flex h-12 shrink-0 items-center gap-2 border-b border-line px-4">
        <h1 className="flex-1 text-[13px] font-semibold text-ink">Admin dashboard</h1>
        <button onClick={onClose} className="icon-btn" aria-label="Close" title="Close (Esc)">
          <CloseIcon className="h-4 w-4" strokeWidth={2.2} />
        </button>
      </header>
      <div className="min-h-0 flex-1">
        <AdminLogin />
      </div>
    </>
  )
}

function Dashboard({
  credential,
  onClose
}: {
  credential: AdminCredential
  onClose: () => void
}): JSX.Element {
  const clearKey = useAdminStore((state) => state.clearKey)
  const signOutAccount = useSessionStore((state) => state.signOut)
  const currentUser = useSessionStore((state) => state.user)

  const data = useDashboardStore((state) => state.data)
  const error = useDashboardStore((state) => state.error)
  const loading = useDashboardStore((state) => state.loading)
  const loadDashboard = useDashboardStore((state) => state.load)
  const clearDashboard = useDashboardStore((state) => state.clear)
  const resetTrafficData = useDashboardStore((state) => state.resetTraffic)

  const [autoRefresh, setAutoRefresh] = useState(true)

  useEffect(() => {
    loadDashboard(credential, false)
    return clearDashboard
  }, [credential, loadDashboard, clearDashboard])

  useEffect(() => {
    if (!autoRefresh) {
      return undefined
    }
    const timer = window.setInterval(() => loadDashboard(credential), REFRESH_MS)
    return () => window.clearInterval(timer)
  }, [autoRefresh, credential, loadDashboard])

  return (
    <>
      <header className="flex h-12 shrink-0 items-center gap-2 border-b border-line px-4">
        <span className="flex h-7 w-7 shrink-0 items-center justify-center rounded-lg bg-brand-soft text-brand">
          <ShieldCheckIcon className="h-4 w-4" />
        </span>
        <h1 className="text-[13px] font-semibold text-ink">Admin dashboard</h1>
        {credential.kind === 'session' ? (
          <span className="rounded-full bg-success/15 px-2 py-0.5 text-[10.5px] font-semibold uppercase tracking-wide text-success">
            admin · {currentUser?.username}
          </span>
        ) : (
          <span
            className="rounded-full bg-warn/15 px-2 py-0.5 text-[10.5px] font-semibold uppercase tracking-wide text-warn"
            title="Using the static X-API-Key: no account stands behind it, so there is no record of who performed each action."
          >
            admin · API key
          </span>
        )}

        <span className="ml-3 min-w-0 truncate text-[11.5px] text-faint">
          {data ? `Updated ${dateTime(data.generatedAt)}` : 'Loading…'}
        </span>

        <div className="ml-auto flex shrink-0 items-center gap-1">
          <label className="mr-1 flex cursor-pointer items-center gap-1.5 text-[11.5px] text-muted">
            <input
              type="checkbox"
              checked={autoRefresh}
              onChange={(event) => setAutoRefresh(event.target.checked)}
              className="accent-[var(--color-brand)]"
            />
            Auto-refresh 10s
          </label>
          <button
            onClick={() => loadDashboard(credential)}
            className="icon-btn"
            aria-label="Refresh"
            title="Refresh now"
          >
            {loading ? (
              <SpinnerIcon className="h-[17px] w-[17px]" />
            ) : (
              <ReloadIcon className="h-[17px] w-[17px]" />
            )}
          </button>
          <button
            onClick={() => resetTrafficData(credential)}
            className="icon-btn hover:text-danger"
            aria-label="Reset traffic metrics"
            title="Reset traffic metrics (leaves the index untouched)"
          >
            <TrashIcon className="h-[17px] w-[17px]" />
          </button>
          <button
            onClick={() => {
              clearKey()
              if (credential.kind === 'session') {
                void signOutAccount()
              }
            }}
            className="icon-btn"
            aria-label="Exit admin mode"
            title="Exit admin mode"
          >
            <ExitIcon className="h-[17px] w-[17px]" />
          </button>
          <div className="mx-1 h-5 w-px bg-line" />
          <button onClick={onClose} className="icon-btn" aria-label="Close" title="Close (Esc)">
            <CloseIcon className="h-4 w-4" strokeWidth={2.2} />
          </button>
        </div>
      </header>

      <div className="min-h-0 flex-1 overflow-y-auto bg-chrome/40 px-5 py-5">
        <div className="mx-auto max-w-6xl">
          {error && (
            <div className="mb-4 flex items-start gap-2.5 rounded-xl border border-warn/30 bg-warn/5 px-3.5 py-2.5">
              <AlertIcon className="mt-0.5 h-4 w-4 shrink-0 text-warn" />
              <p className="text-[12.5px] leading-relaxed text-warn">{error}</p>
            </div>
          )}

          {data === null ? (
            <LoadingSkeleton />
          ) : (
            <div className={loading ? 'opacity-60 transition-opacity' : 'transition-opacity'}>
              <DashboardBody data={data} credential={credential} />
            </div>
          )}
        </div>
      </div>
    </>
  )
}

function DashboardBody({
  data,
  credential
}: {
  data: DashboardDto
  credential: AdminCredential
}): JSX.Element {
  const { traffic, crawl, index, accounts } = data

  const queryCounts = traffic.topQueries.map((item) => item.count)
  const diversity = normalizedEntropy(queryCounts)
  const concentration = headShare(queryCounts, 3)
  const coverage = coverageRatio(crawl.documents, crawl.distinctLinkTargets)
  const danglingShare = ratio(crawl.danglingDocuments, crawl.documents)

  return (
    <div className="flex flex-col gap-5">
      {traffic.truncated && (
        <p className="rounded-xl border border-line bg-raised px-3.5 py-2.5 text-[11.5px] leading-relaxed text-muted">
          One of the stat tables hit its memory ceiling, so the rankings below are missing their tail.
          The totals are still complete. The ceiling is intentional — see{' '}
          <code>UsageAnalyticsService</code>.
        </p>
      )}

      <SectionTitle icon={<ChartIcon className="h-4 w-4" />} text="Usage traffic" />

      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
        <StatTile
          label="Visitors"
          value={compact(traffic.visitors)}
          hint={`${count(traffic.activeVisitors)} active · ${count(traffic.signedInVisitors)} signed in`}
          accent={0}
        />
        <StatTile
          label="Searches"
          value={compact(traffic.searches)}
          hint={`${(traffic.searches / Math.max(1, traffic.visitors)).toFixed(1)} per visitor`}
          accent={1}
        />
        <StatTile
          label="Link clicks"
          value={compact(traffic.clicks)}
          hint={`Click-through rate (CTR) ${percent(traffic.clickThroughRate)}`}
          accent={2}
        />
        <StatTile
          label="Average latency"
          value={millis(traffic.avgLatencyMs)}
          hint="Measured by the server, reported by the UI"
        />
        <StatTile
          label="Pages crawled"
          value={compact(crawl.documents)}
          hint={`${count(crawl.distinctHosts)} domains`}
        />
        <StatTile
          label="Links discovered"
          value={compact(crawl.totalOutlinks)}
          hint={`${compact(crawl.distinctLinkTargets)} distinct targets`}
        />
      </div>

      <ChartCard
        title="Traffic over the last 24 hours"
        subtitle="All three series share the same unit (counts), so they share one axis. Hover or use the ←/→ keys to read hour by hour."
      >
        <TrendChart
          labels={traffic.hourly.map((point) => point.hour)}
          series={[
            { name: 'Sessions', values: traffic.hourly.map((point) => point.visitors) },
            { name: 'Searches', values: traffic.hourly.map((point) => point.searches) },
            { name: 'Clicks', values: traffic.hourly.map((point) => point.clicks) }
          ]}
          emptyText="No activity in the last 24 hours. Try running a few searches and come back."
        />
      </ChartCard>

      <div className="grid gap-5 lg:grid-cols-2">
        <ChartCard
          title="Most popular queries"
          subtitle="Case and whitespace are normalized before grouping."
        >
          <BarList
            rows={traffic.topQueries.map((item) => ({ label: item.label, value: item.count }))}
            emptyText="No queries recorded yet."
          />
        </ChartCard>

        <ChartCard
          title="Links users visited"
          subtitle="With the average rank at click time — the lower the rank, the better the ranker placed it."
        >
          <BarList
            rows={traffic.topLinks.map((item) => ({
              label: shortUrl(item.url),
              value: item.count,
              sub: item.position > 0 ? `avg rank ${item.position.toFixed(1)}` : undefined,
              title: item.url
            }))}
            emptyText="No clicks yet. Click a search result and come back."
          />
        </ChartCard>

        <ChartCard
          title="Query latency distribution"
          subtitle="Buckets grow exponentially, because latency distributions have a very long tail — with even buckets everything piles into the first column."
        >
          <ColumnChart
            data={traffic.latency.map((bucket) => ({
              label: bucket.label,
              value: bucket.count
            }))}
            emptyText="No latency measurements yet."
          />
        </ChartCard>

        <ChartCard
          title="Most clicked domains"
          subtitle="Rolled up from the link table — one domain usually spans many URLs."
        >
          <BarList
            rows={traffic.topHosts.map((item) => ({ label: item.label, value: item.count }))}
            emptyText="No clicks yet."
          />
        </ChartCard>
      </div>

      <SectionTitle icon={<DatabaseIcon className="h-4 w-4" />} text="Crawled data" />

      <div className="grid gap-5 lg:grid-cols-2">
        <ChartCard
          title="Pages crawled per day"
          subtitle={`The last 14 days. Days with no crawling are still shown, so the time axis does not lie.`}
        >
          <ColumnChart
            data={crawl.crawledPerDay.map((day) => ({
              label: dayLabel(day.date),
              value: day.count
            }))}
            labelEvery={2}
            emptyText="The corpus has no crawl timestamps yet."
          />
        </ChartCard>

        <ChartCard
          title="Corpus languages"
          subtitle="Percentage of pages. The tail is folded into “other” instead of being given more colors."
        >
          <ShareBar parts={foldTail(crawl.languages, 3)} emptyText="No pages in the index yet." />
        </ChartCard>

        <ChartCard title="Domains with the most pages in the index">
          <BarList
            rows={crawl.topHosts.map((item) => ({ label: item.label, value: item.count }))}
            emptyText="No pages in the index yet."
          />
        </ChartCard>

        <ChartCard title="Index and corpus">
          <dl className="grid grid-cols-2 gap-x-4 gap-y-2.5 text-[12.5px]">
            <Fact label="Documents in the index" value={count(index.documents)} />
            <Fact label="Distinct terms" value={count(index.terms)} />
            <Fact label="Index file size" value={bytes(index.sizeBytes)} />
            <Fact label="Cache hit rate" value={percent(index.cacheHitRate)} />
            <Fact label="Scorer" value={index.scorer} />
            <Fact
              label="Bloom Filter"
              value={index.bloomFilterBits > 0 ? `${compact(index.bloomFilterBits)} bit` : '—'}
            />
            <Fact label="Oldest crawled page" value={dateTime(crawl.oldestCrawledAt)} />
            <Fact label="Newest crawled page" value={dateTime(crawl.newestCrawledAt)} />
            <Fact label="Average document length" value={`${count(crawl.avgDocLength)} tokens`} />
            <Fact label="Median" value={`${count(crawl.medianDocLength)} tokens`} />
          </dl>
        </ChartCard>
      </div>

      <SectionTitle icon={<ChartIcon className="h-4 w-4" />} text="Analysis" />

      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <StatTile
          label="Query diversity"
          value={percent(diversity, 0)}
          hint="Normalized Shannon entropy over the top table. 0% = everyone searches for the same thing, 100% = evenly spread."
        />
        <StatTile
          label="Top 3 concentration"
          value={percent(concentration, 0)}
          hint="Share of traffic taken by the three largest queries. The heavier the head, the more effective the LRU cache."
        />
        <StatTile
          label="Zero-result queries"
          value={percent(traffic.zeroResultRate, 0)}
          hint={`${count(traffic.zeroResultSearches)} searches. High means the index is missing content, not that users mistyped.`}
        />
        <StatTile
          label="Average session"
          value={`${traffic.avgSessionMinutes.toFixed(1)} min`}
          hint="From the first to the last event of the same session."
        />
        <StatTile
          label="Crawl coverage"
          value={percent(coverage, 1)}
          hint="Pages fetched / link targets seen. A low value is normal when maxPages is capped."
        />
        <StatTile
          label="Dangling pages"
          value={percent(danglingShare, 1)}
          hint={`${count(crawl.danglingDocuments)} pages with no outgoing links. PageRank has to handle this group separately.`}
        />
        <StatTile
          label="Average links per page"
          value={crawl.avgOutlinks.toFixed(1)}
          hint="Average out-degree of the crawled web graph."
        />
        <StatTile
          label="Clicks per search"
          value={traffic.clickThroughRate.toFixed(2)}
          hint="Below 1 is normal: many queries end without anyone clicking anything."
        />
      </div>

      <SectionTitle icon={<UsersIcon className="h-4 w-4" />} text="Accounts" />

      <div className="grid grid-cols-2 gap-3 md:grid-cols-4">
        <StatTile label="Total accounts" value={count(accounts.total)} />
        <StatTile
          label="Administrators"
          value={count(accounts.admins)}
          hint={
            accounts.admins === 0
              ? 'No ADMIN account yet — right now the only way in is the API key.'
              : undefined
          }
        />
        <StatTile
          label="Open sign-in sessions"
          value={count(accounts.activeSessions)}
          hint="Tokens that are still valid. Different from the analytics sessions above."
        />
        <StatTile
          label="Disabled accounts"
          value={count(accounts.disabled)}
          hint="Locked out, but their data is kept intact."
        />
      </div>

      <ChartCard
        title="Account list"
        subtitle="Roles are read straight from the server. This answers “which accounts are admins and which are regular users”."
      >
        <AccountsTable credential={credential} />
      </ChartCard>

      <ChartCard
        title="Accounts that search the most"
        subtitle="Names and counts only — individual queries are deliberately NOT included. This table answers “who uses it a lot”, not “what did this person search for”."
      >
        <BarList
          rows={traffic.topUsers.map((item) => ({ label: item.label, value: item.count }))}
          emptyText="No searches from signed-in users yet. Anonymous users are not attributed to any account."
        />
      </ChartCard>

      <SectionTitle icon={<ShieldCheckIcon className="h-4 w-4" />} text="Access permissions" />
      <div className="rounded-2xl border border-line bg-surface p-4">
        <PermissionMatrix />
      </div>
    </div>
  )
}

function SectionTitle({ icon, text }: { icon: JSX.Element; text: string }): JSX.Element {
  return (
    <div className="mt-1 flex items-center gap-2 text-muted">
      {icon}
      <h2 className="text-[12px] font-semibold uppercase tracking-wide">{text}</h2>
      <div className="h-px flex-1 bg-line" />
    </div>
  )
}

function Fact({ label, value }: { label: string; value: string }): JSX.Element {
  return (
    <div className="min-w-0">
      <dt className="truncate text-[11.5px] text-faint">{label}</dt>
      <dd className="truncate text-ink">{value}</dd>
    </div>
  )
}

function LoadingSkeleton(): JSX.Element {
  return (
    <div className="flex flex-col gap-5" aria-hidden="true">
      <div className="grid grid-cols-2 gap-3 md:grid-cols-3 xl:grid-cols-6">
        {Array.from({ length: 6 }).map((_, i) => (
          <div key={i} className="skeleton h-[86px] rounded-2xl" />
        ))}
      </div>
      <div className="skeleton h-64 rounded-2xl" />
      <div className="grid gap-5 lg:grid-cols-2">
        <div className="skeleton h-72 rounded-2xl" />
        <div className="skeleton h-72 rounded-2xl" />
      </div>
    </div>
  )
}

export default AdminPanel
