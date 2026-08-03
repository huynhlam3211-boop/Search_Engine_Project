import {useEffect, useRef, useState} from 'react'
import {useTabStore, HOME_URL} from '../store/tabStore'
import {useBookmarkStore} from '../store/bookmarkStore'
import {useSearchViewScore} from '../store/searchViewStore'
import AutocompleteDropdown from './AutocompleteDropdown'
import {suggest} from '../lib/searchApi'
import {CloseIcon, GlobeIcon, LockIcon, TransitionStartFunction, VnSearchMark} from './icon'

function lookslikeUrl(text: string): boolean {

}

function AddressBar(): JSX.Element {



 function handleKeyDown(): void {

 }

 function handleSubmit(e: React.FormEvent): void {

 }

 return (

 ) 

}

export default AddressBar
