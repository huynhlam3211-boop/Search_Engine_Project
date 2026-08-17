import { describe, expect, it } from 'vitest'
import { BookmarkTrie } from './BookmarkTrie'

describe('BookmarkTrie', () => {
  it('finds entries by prefix', () => {
    const trie = new BookmarkTrie()
    trie.insert('vnexpress', 'bm-1')
    trie.insert('vnedu', 'bm-2')
    trie.insert('tuoitre', 'bm-3')

    expect(trie.searchByPrefix('vn').sort()).toEqual(['bm-1', 'bm-2'])
    expect(trie.searchByPrefix('vne').sort()).toEqual(['bm-1', 'bm-2'])
    expect(trie.searchByPrefix('vnex')).toEqual(['bm-1'])
    expect(trie.searchByPrefix('tu')).toEqual(['bm-3'])
  })

  it('returns an empty array when no prefix matches', () => {
    const trie = new BookmarkTrie()
    trie.insert('vnexpress', 'bm-1')

    expect(trie.searchByPrefix('zz')).toEqual([])
    expect(trie.searchByPrefix('vnexpressssss')).toEqual([])
  })

  it('an empty prefix returns EVERYTHING', () => {
    const trie = new BookmarkTrie()
    trie.insert('a', 'bm-1')
    trie.insert('b', 'bm-2')

    expect(trie.searchByPrefix('').sort()).toEqual(['bm-1', 'bm-2'])
  })

  it('is case insensitive on both insert and search', () => {
    const trie = new BookmarkTrie()
    trie.insert('VnExpress', 'bm-1')

    expect(trie.searchByPrefix('vnexp')).toEqual(['bm-1'])
    expect(trie.searchByPrefix('VNEXP')).toEqual(['bm-1'])
  })

  it('handles accented Vietnamese text correctly', () => {
    const trie = new BookmarkTrie()
    trie.insert('máy tính', 'bm-1')
    trie.insert('màn hình', 'bm-2')

    expect(trie.searchByPrefix('má')).toEqual(['bm-1'])
    expect(trie.searchByPrefix('màn')).toEqual(['bm-2'])
    expect(trie.searchByPrefix('m').sort()).toEqual(['bm-1', 'bm-2'])
  })

  it('merges several bookmarks under the same keyword without duplicates', () => {
    const trie = new BookmarkTrie()
    trie.insert('news', 'bm-1')
    trie.insert('news', 'bm-2')
    trie.insert('news', 'bm-1')

    expect(trie.searchByPrefix('news').sort()).toEqual(['bm-1', 'bm-2'])
  })

  it('skips empty keywords instead of creating junk nodes', () => {
    const trie = new BookmarkTrie()
    trie.insert('', 'bm-junk')
    trie.insert('real', 'bm-real')

    expect(trie.searchByPrefix('')).toEqual(['bm-real'])
  })

  it('a word that is both a complete word and a prefix of another', () => {
    const trie = new BookmarkTrie()
    trie.insert('news', 'bm-short')
    trie.insert('news feed', 'bm-long')

    expect(trie.searchByPrefix('news').sort()).toEqual(['bm-long', 'bm-short'])
  })
})
