export interface Bookmark {
  url: string
  title: string
}

class TrieNode {
  readonly children = new Map<string, TrieNode>()
  /** Cac bookmark ket thuc dung tai nut nay. */
  readonly items: Bookmark[] = []
}

/**
 * Trie tim bookmark theo tien to, dung cho goi y trong o dia chi.
 * Khoa la title + url viet thuong, moi bookmark duoc chen theo ca hai khoa
 * de go "yout..." hay "https://you..." deu ra.
 */
export class BookmarkTrie {
  private readonly root = new TrieNode()
  private size = 0

  insert(bookmark: Bookmark): void {
    for (const key of BookmarkTrie.keysOf(bookmark)) {
      let node = this.root
      for (const ch of key) {
        let next = node.children.get(ch)
        if (!next) {
          next = new TrieNode()
          node.children.set(ch, next)
        }
        node = next
      }
      node.items.push(bookmark)
    }
    this.size++
  }

  get count(): number {
    return this.size
  }

  searchByPrefix(prefix: string, limit = 8): Bookmark[] {
    const key = prefix.trim().toLowerCase()
    if (!key) return []

    let node = this.root
    for (const ch of key) {
      const next = node.children.get(ch)
      if (!next) return []
      node = next
    }

    const out: Bookmark[] = []
    const seen = new Set<string>()
    this.collect(node, out, seen, limit)
    return out
  }

  private collect(node: TrieNode, out: Bookmark[], seen: Set<string>, limit: number): void {
    if (out.length >= limit) return
    for (const item of node.items) {
      // Mot bookmark nam duoi nhieu khoa nen phai khu trung theo url.
      if (seen.has(item.url)) continue
      seen.add(item.url)
      out.push(item)
      if (out.length >= limit) return
    }
    for (const child of node.children.values()) {
      this.collect(child, out, seen, limit)
      if (out.length >= limit) return
    }
  }

  static from(bookmarks: Bookmark[]): BookmarkTrie {
    const trie = new BookmarkTrie()
    bookmarks.forEach((b) => trie.insert(b))
    return trie
  }

  private static keysOf(bookmark: Bookmark): string[] {
    const url = bookmark.url.toLowerCase()
    const bare = url.replace(/^https?:\/\/(www\.)?/, '')
    return [bookmark.title.toLowerCase(), url, bare]
  }
}
