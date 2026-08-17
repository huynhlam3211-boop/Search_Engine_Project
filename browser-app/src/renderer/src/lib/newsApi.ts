import { getJson } from './searchApi'

export interface FeedCard {
  url: string
  title: string
  snippet: string
  imageUrl: string
  altText: string
  host: string
}

export interface FeedResponse {
  results: FeedCard[]
  page: number
  pageSize: number
  totalResults: number
  hasMore: boolean
  indexedDocuments: number
}

const FEED_SEED = Math.floor(Math.random() * 1_000_000)

export async function fetchFeed(page = 1, size = 12): Promise<FeedResponse> {
  const raw = await getJson<Partial<FeedResponse>>('/api/feed', {
    seed: FEED_SEED,
    page,
    size
  })

  const results = (raw.results ?? []).filter(
    (card): card is FeedCard => Boolean(card?.url) && Boolean(card?.imageUrl)
  )

  return {
    results,
    page: raw.page ?? page,
    pageSize: raw.pageSize ?? size,
    totalResults: raw.totalResults ?? results.length,
    hasMore: raw.hasMore ?? false,
    indexedDocuments: raw.indexedDocuments ?? 0
  }
}
