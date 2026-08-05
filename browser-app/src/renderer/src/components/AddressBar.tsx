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
import { CloseIcon, GlobeIcon, LockIcon, StarIcon, VnSearchMark } from './icon'

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
  const inputRef = useRef<HTMLInputElement>(null)
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
    <form onSubmit={handleSubmit} className="relative flex min-w-0 flex-1 items-center gap-1.5">
      <div
        className={
          'flex h-10 min-w-0 flex-1 items-center gap-2.5 rounded-full border px-3.5 transition-all duration-200 ' +
          (focused
            ? 'border-brand/45 bg-omni shadow-omni ring-4 ring-brand/10'
            : 'border-transparent bg-omni hover:brightness-110')
        }
      >
        <span className="flex shrink-0 items-center">
            {searchMode ? (
              <VnSearchMark className="h-[18px] w-[18px] text-muted" />
            ) : isSecure ? (
              <LockIcon className ="h-[15px] w-[15px] text-success" />
            ) : (
              <GlobeIcon className="h-[16px] w-[16px] text-warn" />
            )} 
        </span>


        <input
            id="omnibox"
            ref={inputRef}
            value={inputValue}
            onChange={(e) => {
              setInputValue(e.target.value)
              setHighlighted(-1)
            }}
            onKeyDown={handleKeyDown}
            onFocus={(e) => {
              setFocused(true)
              e.target.select()
            }}
            onBlur={() => {
              setFocused(false)
              setHighlighted(-1)
            }}
            className ="min-w-0 flex-1 bg-transparent text-[13.5px] text-ink placeholder:text-faint focus:outline-none"
            placeholder="Tìm kiếm hoặc nhập địa chỉ web"
            spellCheck={false}
            aria-label="Ô địa chỉ và tìm kiếm"
        />

        {inputValue && (
          <button
            type="button"
            onClick={() => {
              setInputValue('')
              setSuggestions([])
              inputRef.current?.focus()
            }}
            className="flex h-5 w-5 shrink-0 items-center justify-center rounded-full text-faint transition hover:bg-line hover:text-ink"
            aria-label="Xoá nội dung"
            title="Xoá"
          > 
            <CloseIcon className="h-3 w-3" strokeWidth={2.2} />
          </button>
        )}

      </div>


      <button
        type="button"
        onClick={() => {
          if (activeTab && activeTab.url !== HOME_URL) {
            toggleBookmark(activeTab.url, activeTab.title)
          }
        }}
        disabled={!activeTab || activeTab.url === HOME_URL}
        className={'icon-btn ' + (bookmarked ? 'text-amber-500 hover:text-amber-500' : '')}
        aria-label="Đánh dấu trang"
        title={bookmarked ? 'Bỏ đánh dấu' : 'Đánh dấu trang (Ctrl+D)'}
      >
          <StarIcon className="h-[18px] w-[18px]" filled={bookmarked} />
      </button>

      <AutocompleteDropdown
        items={suggestions}
        highlighted={highlighted}
        onPick={(s) => {
          setInputValue(s)
          run(s)
        }}
        onHover={setHighlighted}
      />
    </form> 
  )
}

export default AddressBar
