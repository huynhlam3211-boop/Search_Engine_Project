import { SEED_SITES } from './seedSites'

export const API_BASE = 'http://localhost:8080'

export interface SearchResultDto {
  url: string
  title: string
  snippet: string
  score?: number
}

export interface SearchResponse {
  items: SearchResultDto[]
  total: number
  tookMs: number
  /** true = ket qua gia lap vi backend chua tra loi. */
  mock: boolean
}

/**
 * BAN VA TAM THOI.
 *
 * Backend (search-engine) hien moi co phan crawler, chua co REST controller nao,
 * nen /api/search chac chan 404. Ham nay van goi that truoc — de khi ban them
 * controller thi frontend tu dong dung du lieu that — va chi rot xuong du lieu
 * gia lap khi goi that hong. Co `mock` trong ket qua de giao dien noi ro cho
 * nguoi dung biet dang xem so lieu gia.
 */

let backendDown = false
let backendCheckedAt = 0
const RECHECK_MS = 15_000

async function fetchJson<T>(path: string, timeoutMs = 1500): Promise<T> {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), timeoutMs)
  try {
    const res = await fetch(`${API_BASE}${path}`, { signal: controller.signal })
    if (!res.ok) throw new Error(`HTTP ${res.status}`)
    return (await res.json()) as T
  } finally {
    clearTimeout(timer)
  }
}

function backendLikelyDown(): boolean {
  if (!backendDown) return false
  // Sau RECHECK_MS thi thu lai, phong khi backend vua duoc bat len.
  if (Date.now() - backendCheckedAt > RECHECK_MS) {
    backendDown = false
    return false
  }
  return true
}

function markBackendDown(): void {
  backendDown = true
  backendCheckedAt = Date.now()
}

export async function search(query: string, page = 0): Promise<SearchResponse> {
  const started = performance.now()

  if (!backendLikelyDown()) {
    try {
      const data = await fetchJson<{ items?: SearchResultDto[]; total?: number; tookMs?: number }>(
        `/api/search?q=${encodeURIComponent(query)}&page=${page}`
      )
      return {
        items: data.items ?? [],
        total: data.total ?? data.items?.length ?? 0,
        tookMs: data.tookMs ?? Math.round(performance.now() - started),
        mock: false
      }
    } catch {
      markBackendDown()
    }
  }

  const items = mockSearch(query)
  return {
    items,
    total: items.length,
    tookMs: Math.round(performance.now() - started),
    mock: true
  }
}

export async function suggest(prefix: string): Promise<string[]> {
  const key = prefix.trim().toLowerCase()
  if (!key) return []

  if (!backendLikelyDown()) {
    try {
      const data = await fetchJson<string[] | { suggestions?: string[] }>(
        `/api/suggest?q=${encodeURIComponent(key)}`,
        800
      )
      return Array.isArray(data) ? data : (data.suggestions ?? [])
    } catch {
      markBackendDown()
    }
  }

  return mockSuggest(key)
}

/** Kiem tra backend co song khong — dung cho chi bao trang thai o thanh ben. */
export async function ping(): Promise<boolean> {
  try {
    await fetchJson('/api/admin/stats', 1200)
    backendDown = false
    return true
  } catch {
    markBackendDown()
    return false
  }
}

// --------------------------------------------------------------------------
// Du lieu gia lap — xoa toan bo phan duoi khi backend co /api/search that.
// --------------------------------------------------------------------------

interface MockDoc {
  url: string
  title: string
  body: string
}

const MOCK_CORPUS: MockDoc[] = [
  ...SEED_SITES.map((site) => ({
    url: site.url,
    title: site.title,
    body: `${site.title} — ${site.tags.join(', ')}. Trang chủ ${site.url}.`
  })),
  {
    url: 'https://vi.wikipedia.org/wiki/Máy_tìm_kiếm',
    title: 'Máy tìm kiếm – Wikipedia tiếng Việt',
    body: 'Máy tìm kiếm là hệ thống thu thập trang web bằng trình thu thập (crawler), lập chỉ mục ngược và xếp hạng kết quả theo độ liên quan.'
  },
  {
    url: 'https://vi.wikipedia.org/wiki/Bộ_lọc_Bloom',
    title: 'Bộ lọc Bloom – Wikipedia tiếng Việt',
    body: 'Bộ lọc Bloom là cấu trúc dữ liệu xác suất, tiết kiệm bộ nhớ, dùng để kiểm tra một phần tử đã xuất hiện hay chưa; có thể báo dương tính giả nhưng không âm tính giả.'
  },
  {
    url: 'https://vi.wikipedia.org/wiki/PageRank',
    title: 'PageRank – thuật toán xếp hạng trang',
    body: 'PageRank xếp hạng trang web dựa trên cấu trúc liên kết, coi mỗi liên kết là một lá phiếu và tính vector riêng của ma trận chuyển.'
  },
  {
    url: 'https://vnexpress.net/khoa-hoc',
    title: 'Khoa học - VnExpress',
    body: 'Tin khoa học công nghệ, nghiên cứu mới, vũ trụ, trí tuệ nhân tạo cập nhật hằng ngày.'
  },
  {
    url: 'https://tuoitre.vn/giao-duc.htm',
    title: 'Giáo dục - Tuổi Trẻ Online',
    body: 'Tin tức giáo dục, tuyển sinh đại học, đề thi và điểm chuẩn các trường.'
  },
  {
    url: 'https://github.com/topics/web-crawler',
    title: 'web-crawler · GitHub Topics',
    body: 'Các dự án mã nguồn mở về trình thu thập dữ liệu web, hàng đợi URL, chuẩn hoá URL và tuân thủ robots.txt.'
  },
  {
    url: 'https://stackoverflow.com/questions/tagged/lucene',
    title: 'Câu hỏi gắn thẻ lucene - Stack Overflow',
    body: 'Hỏi đáp về Apache Lucene, chỉ mục ngược, phân tích văn bản và tính điểm BM25.'
  }
]

/** Bo dau tieng Viet de go khong dau van tim duoc. */
function normalize(text: string): string {
  return text
    .toLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '')
    .replace(/đ/g, 'd')
}

function mockSearch(query: string): SearchResultDto[] {
  const terms = normalize(query).split(/\s+/).filter(Boolean)
  if (terms.length === 0) return []

  return MOCK_CORPUS.map((doc) => {
    const haystack = normalize(`${doc.title} ${doc.body} ${doc.url}`)
    // Cham diem tho: dem so lan xuat hien, tieu de tinh diem gap doi.
    let score = 0
    for (const term of terms) {
      const inTitle = normalize(doc.title).includes(term)
      const hits = haystack.split(term).length - 1
      score += hits + (inTitle ? 2 : 0)
    }
    return { doc, score }
  })
    .filter((r) => r.score > 0)
    .sort((a, b) => b.score - a.score)
    .map(({ doc, score }) => ({
      url: doc.url,
      title: doc.title,
      snippet: doc.body,
      score: Number(score.toFixed(2))
    }))
}

function mockSuggest(prefix: string): string[] {
  const key = normalize(prefix)
  const pool = [
    ...MOCK_CORPUS.map((d) => d.title),
    ...SEED_SITES.flatMap((s) => s.tags),
    'bộ lọc bloom là gì',
    'cách hoạt động của máy tìm kiếm',
    'chỉ mục ngược inverted index',
    'thuật toán pagerank',
    'crawler tuân thủ robots.txt'
  ]
  return Array.from(new Set(pool.filter((s) => normalize(s).includes(key)))).slice(0, 8)
}
