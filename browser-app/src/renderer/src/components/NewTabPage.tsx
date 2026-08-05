import { useEffect, useState, type JSX } from 'react'
import { useTabStore } from '../store/tabStore'
import { useSearchViewStore } from '../store/searchViewStore'
import { SEED_SITES } from '../lib/seedSites'
import { fetchHotNews, type NewsCard } from '../lib/newsApi'
import { ping } from '../lib/searchApi'
import { SearchIcon, VnSearchMark } from './icon'

function NewTabPage(): JSX.Element {
  const navigate = useTabStore((s) => s.navigate)
  const runSearch = useSearchViewStore((s) => s.runSearch)
  const [text, setText] = useState('')
  const [news, setNews] = useState<NewsCard[]>([])
  const [backendUp, setBackendUp] = useState<boolean | null>(null)

  useEffect(() => {
    ping().then(setBackendUp)
    fetchHotNews().then(setNews)
  }, [])

  return (
    <div className="newtab">
    </div>
  )
}

export default NewTabPage
