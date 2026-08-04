import {useEffect, useRef, useState} from 'react'
import {useTabStore, HOME_URL} from '../store/tabStore'
import {useBookmarkStore} from '../store/bookmarkStore'
import {useSearchViewStore} from '../store/searchViewStore'
import AutocompleteDropdown from './AutocompleteDropdown'
import {suggest} from '../lib/searchApi'
import {CloseIcon, GlobeIcon, LockIcon, TransitionStartFunction, VnSearchMark} from './icon'


/** Nhận diện một chuỗi gõ vào là URL */
function lookslikeUrl(text: string): boolean {
    const trimmed = text.trim()
    if (/^http?:\/\//i.test(trimmed)){
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
    const isBookmarked = useBookmarkStore((s) => s.isBookmarked)
    const toggleBookmark = useBookmarkStore((s) => s.toggleBookmark)

    useBookmarkStore((s) => s.root)

    const activeTab = tabs.find((t) => t.id === activeTabId)
    const displayedUrl = activeTab?.url === HOME_URL ? '': (activeTab?.url ?? '')
    const [inputValue, setIn]


 function handleKeyDown(): void {

 }

 function handleSubmit(e: React.FormEvent): void {

 }

 return (

 ) 

}

export default AddressBar
