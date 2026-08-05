import { search, type SearchResultDto } from './searchApi'

const TOPICS = ['thời sự', 'khoa học', 'giáo dục', 'công nghệ'] as const

export interface NewsCard {
  topic: string
  title: string
  url: string
  snippet: string
  source: string
}

function toCard(topic: string, dto: SearchResultDto): NewsCard {
  let source = dto.url
  try {
    source = new URL(dto.url).hostname.replace(/^www\./, '')
  } catch {
    // URL hong thi cu hien nguyen chuoi.
  }
  return { topic, title: dto.title, url: dto.url, snippet: dto.snippet, source }
}

/**
 * Tin noi bat cho trang chu. Hien tai ghep tu ket qua tim kiem theo vai chu de;
 * khi backend co endpoint tin tuc rieng thi thay phan than ham nay.
 */
export async function fetchHotNews(limit = 6): Promise<NewsCard[]> {
  const batches = await Promise.all(
    TOPICS.map(async (topic) => {
      try {
        const res = await search(topic)
        return res.items.slice(0, 2).map((item) => toCard(topic, item))
      } catch {
        return []
      }
    })
  )

  const seen = new Set<string>()
  const cards: NewsCard[] = []
  for (const card of batches.flat()) {
    if (seen.has(card.url)) continue
    seen.add(card.url)
    cards.push(card)
  }
  return cards.slice(0, limit)
}
