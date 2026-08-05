import {
  useEffect,
  useRef,
  useState,
  type JSX,
  type KeyboardEvent,
  type SyntheticEvent
} from 'react'
import { useTabStore, HOME_URL } from '../store/tabStore'
import { useBookmarkStore } from '../store/bookmarkStore'
import { useSearchViewStore } from '../store/searchViewStore'
import AutocompleteDropdown from './AutocompleteDropdown'
import { suggest } from '../lib/searchApi'
import { OMNIBOX_ID } from '../lib/useBrowserShortcuts'
import { CloseIcon, GlobeIcon, LockIcon, SearchIcon, StarIcon } from './icon'

function looksLikeUrl(text: string): boolean {
  const trimmed = text.trim()
  if (/^https?:\/\//i.test(trimmed)) {
    return true
  }
  return !trimmed.includes(' ') && /\.[a-z]{2,}(\/.*)?$/i.test(trimmed)
}

function AddressBar(): JSX.Element {
  const tabs = useTabStore((s) => s.tabs)
  const activeTabId = useTabStore((s) => s.activeTabId)
  const navigate = useTabStore((s) => s.navigate)
  const query = useSearchViewStore((s) => s.query)
  const setQuery = useSearchViewStore((s) => s.setQuery)
  const runSearch = useSearchViewStore((s) => s.runSearch)
  const isBookmarked = useBookmarkStore((s) => s.isBookmarked)
  const toggleBookmark = useBookmarkStore((s) => s.toggleBookmark)

  useBookmarkStore((s) => s.root)

  const activeTab = tabs.find((t) => t.id === activeTabId)
  const displayedUrl = activeTab?.url === HOME_URL ? '' : (activeTab?.url ?? '')
  const [inputValue, setInputValue] = useState(query || displayedUrl)
  const [focused, setFocused] = useState(false)
  const [suggestions, setSuggestions] = useState<string[]>([])
  const [highlighted, setHighlighted] = useState(-1)
  const debounceRef = useRef<number | undefined>(undefined)

  useEffect(() => {
    if (focused) return
    setInputValue(query || displayedUrl)
  }, [query, displayedUrl, focused])

  useEffect(() => {
    if (!focused || inputValue.trim().length < 2 || looksLikeUrl(inputValue)) {
      setSuggestions([])
      return
    }
    window.clearTimeout(debounceRef.current)
    debounceRef.current = window.setTimeout(() => {
      suggest(inputValue).then(setSuggestions)
    }, 180)

    return () => window.clearTimeout(debounceRef.current)
  }, [inputValue, focused])

  const bookmarked = activeTab && activeTab.url !== HOME_URL ? isBookmarked(activeTab.url) : false
  const isSecure = /^https:\/\//i.test(displayedUrl)
  const searchMode = !displayedUrl || !!query

  function run(text: string): void {
    const value = text.trim()
    if (!value) return

    setSuggestions([])
    setHighlighted(-1)

    if (looksLikeUrl(value)) {
      useSearchViewStore.getState().clear()
      navigate(/^https?:\/\//i.test(value) ? value : `https://${value}`)
    } else {
      navigate(HOME_URL)
      runSearch(value)
    }
    ;(document.activeElement as HTMLElement | null)?.blur()
  }

  function handleKeyDown(e: KeyboardEvent<HTMLInputElement>): void {
    if (e.key === 'ArrowDown' && suggestions.length > 0) {
      e.preventDefault()
      setHighlighted((h) => (h + 1) % suggestions.length)
    } else if (e.key === 'ArrowUp' && suggestions.length > 0) {
      e.preventDefault()
      setHighlighted((h) => (h <= 0 ? suggestions.length - 1 : h - 1))
    } else if (e.key === 'Escape') {
      setSuggestions([])
      setHighlighted(-1)
      setInputValue(query || displayedUrl)
    }
  }

  function handleSubmit(e: SyntheticEvent): void {
    e.preventDefault()
    run(highlighted >= 0 ? suggestions[highlighted] : inputValue)
  }

  return (
    <form onSubmit={handleSubmit} className="relative flex min-w-0 flex-1 items-center">
      <div
        className={
          'flex h-9 min-w-0 flex-1 items-center gap-2.5 rounded-full border bg-omni px-3.5 transition-all duration-200 ' +
          (focused
            ? 'border-brand/45 shadow-omni ring-4 ring-brand/10'
            : 'border-transparent hover:brightness-110')
        }
      >
        <span className="flex text-ink-dim">
          {searchMode ? <SearchIcon /> : isSecure ? <LockIcon /> : <GlobeIcon />}
        </span>

        <input
          id={OMNIBOX_ID}
          className="min-w-0 flex-1 select-text bg-transparent text-ink outline-none placeholder:text-ink-dim/70"
          value={inputValue}
          placeholder="Tìm trên VnSearch hoặc nhập địa chỉ"
          spellCheck={false}
          autoComplete="off"
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={handleKeyDown}
          onFocus={(e) => {
            setFocused(true)
            e.target.select()
          }}
          onBlur={() => {
            setFocused(false)
            setSuggestions([])
            setHighlighted(-1)
          }}
        />

        {inputValue.length > 0 && (
          <button
            type="button"
            className="flex rounded-full p-1 text-ink-dim hover:bg-white/10 hover:text-ink"
            title="Xoá"
            // Giu tieu diem o input, neu khong blur se chay truoc onClick.
            onMouseDown={(e) => e.preventDefault()}
            onClick={() => {
              setInputValue('')
              setQuery('')
            }}
          >
            <CloseIcon />
          </button>
        )}

        {activeTab && activeTab.url !== HOME_URL && (
          <button
            type="button"
            className={
              'flex rounded-full p-1 hover:bg-white/10 ' +
              (bookmarked ? 'text-amber-400' : 'text-ink-dim hover:text-ink')
            }
            title={bookmarked ? 'Bỏ dấu trang' : 'Thêm dấu trang'}
            onMouseDown={(e) => e.preventDefault()}
            onClick={() => toggleBookmark(activeTab.url, activeTab.title)}
          >
            <StarIcon filled={bookmarked} />
          </button>
        )}
      </div>

      {focused && (
        <AutocompleteDropdown
          items={suggestions}
          highlighted={highlighted}
          onPick={run}
          onHover={setHighlighted}
        />
      )}
    </form>
  )
}

export default AddressBar
