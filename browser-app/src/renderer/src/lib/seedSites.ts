export interface SeedSite {
  title: string
  url: string
  /** Chu cai hien trong o vuong khi khong tai duoc favicon. */
  letter: string
  color: string
  tags: string[]
}

/**
 * Danh sach trang goc, dung cho phim tat o trang chu va cho ket qua tim kiem
 * gia lap trong lib/searchApi.ts khi backend chua chay.
 */
export const SEED_SITES: SeedSite[] = [
  {
    title: 'VnExpress',
    url: 'https://vnexpress.net',
    letter: 'V',
    color: '#c0392b',
    tags: ['tin tức', 'báo', 'thời sự', 'vnexpress']
  },
  {
    title: 'Tuổi Trẻ Online',
    url: 'https://tuoitre.vn',
    letter: 'T',
    color: '#1e6fd9',
    tags: ['tin tức', 'báo', 'tuổi trẻ']
  },
  {
    title: 'Thanh Niên',
    url: 'https://thanhnien.vn',
    letter: 'T',
    color: '#0a7a4b',
    tags: ['tin tức', 'báo', 'thanh niên']
  },
  {
    title: 'Dân Trí',
    url: 'https://dantri.com.vn',
    letter: 'D',
    color: '#2c6ebd',
    tags: ['tin tức', 'báo', 'dân trí']
  },
  {
    title: 'VietnamNet',
    url: 'https://vietnamnet.vn',
    letter: 'V',
    color: '#e0651a',
    tags: ['tin tức', 'báo', 'vietnamnet']
  },
  {
    title: 'Wikipedia tiếng Việt',
    url: 'https://vi.wikipedia.org',
    letter: 'W',
    color: '#4a4a4a',
    tags: ['bách khoa', 'tra cứu', 'wikipedia']
  },
  {
    title: 'GitHub',
    url: 'https://github.com',
    letter: 'G',
    color: '#24292f',
    tags: ['lập trình', 'mã nguồn', 'github', 'git']
  },
  {
    title: 'Stack Overflow',
    url: 'https://stackoverflow.com',
    letter: 'S',
    color: '#e07a26',
    tags: ['lập trình', 'hỏi đáp', 'stackoverflow']
  }
]
