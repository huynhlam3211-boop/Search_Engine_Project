import type { JSX } from 'react'
import { useSearchViewStore } from '../store/searchViewStore'
import { useTabStore } from '../store/tabStore'

function hostOf(url: string): string {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return url
  }
}

function SearchResultList(): JSX.Element {
  const query = useSearchViewStore((s) => s.query)
  const results = useSearchViewStore((s) => s.results)
  const total = useSearchViewStore((s) => s.total)
  const tookMs = useSearchViewStore((s) => s.tookMs)
  const status = useSearchViewStore((s) => s.status)
  const error = useSearchViewStore((s) => s.error)
  const usedMock = useSearchViewStore((s) => s.usedMock)
  const navigate = useTabStore((s) => s.navigate)

  return (
    <div className="serp">
    </div>
  )
}

export default SearchResultList
