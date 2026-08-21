# VnSearch Crawler

Web crawler đa luồng, đa domain cho tiếng Việt và tiếng Anh. Đầu ra là một corpus
JSON (văn bản + đồ thị liên kết + ảnh đại diện mỗi trang) dùng làm đầu vào cho
phần đánh chỉ mục và tìm kiếm.

> **Phạm vi của README này và của nhánh hiện tại.** Chỉ phần crawler được biên
> dịch. `pom.xml` giới hạn `maven-compiler-plugin` vào ba package:
> `com/vnsearch/crawler/**`, `com/vnsearch/datastructure/**`, `com/vnsearch/model/**`.
> Các package `index`, `query`, `ranking`, `storage`, `auth`, `config`,
> `controller`, `service` còn là file rỗng hoặc đang viết dở nên không nằm trong
> phạm vi build. Bỏ hai thẻ `<includes>` / `<testIncludes>` khi các phần đó xong.

- **Java**: 21 (`maven.compiler.release=21`)
- **Phụ thuộc chính**: jsoup (tải + phân tích HTML), Jackson (JSON), Micrometer
  (số liệu), Spring Kafka (chỉ dùng cho chế độ nhiều tiến trình)
- **Trạng thái test**: `272 tests, 0 failures` (`mvnw test`)

---

## 1. Chạy nhanh

Từ thư mục gốc repo (Windows):

```bat
run-crawl.bat [maxPages] [maxDepth] [output] [--fresh]
```

Mặc định của `run-crawl.bat`: `10000` trang, độ sâu `4`, ghi ra
`data/crawled-documents.json` (đường dẫn tương đối với `search-engine/`).

```bat
rem crawl mới hoặc NỐI TIẾP corpus sẵn có tại đường dẫn output
run-crawl.bat 5000 3 data/crawled-documents.json

rem xoá corpus cũ và crawl lại từ đầu (script hỏi xác nhận, phải gõ XOA)
run-crawl.bat 5000 3 data/crawled-documents.json --fresh
```

Không dùng file `.bat` thì gọi thẳng runner:

```bash
cd search-engine
./mvnw -q compile exec:java \
  -Dexec.mainClass=com.vnsearch.crawler.MultiDomainCrawlRunner \
  -Dexec.args="5000 3 data/crawled-documents.json"
```

Tham số của `MultiDomainCrawlRunner`: `[maxPages=5000] [maxDepth=3]
[output=data/crawled-multi.json] [--fresh]`.

Xem thống kê corpus vừa crawl:

```bat
crawl-stats.bat "search-engine/data/crawled-documents.json"
```

Ctrl+C giữa chừng vẫn an toàn: checkpoint được ghi mỗi 250 trang vào đúng tệp
đầu ra, và lần chạy sau tự nối tiếp từ đó.

### Biến môi trường / system property

| Tên | Giá trị | Tác dụng |
| --- | --- | --- |
| `CRAWL_PROGRESS` | `bar` (mặc định) | Ép hiển thị thanh tiến độ; giá trị khác thì in từng dòng |
| `-Dcrawl.progress` | `bar` | Như trên, dạng system property |
| `NO_COLOR` | có đặt | Tắt màu ANSI trên thanh tiến độ |

---

## 2. Kiến trúc

Mỗi khối trong sơ đồ là **một lớp riêng**; `CrawlerService` chỉ nối chúng lại và
không tự làm việc gì.

```
   seed URLs
       |
       v
   URL Frontier -> HTML Downloader -> Content Parser -> Language Filter -> Content Seen? -(Yes)-> vứt
       ^                 |                            (không vi/en) vứt        |
       |                 v                                                     | (No)
       |           DNS Resolver                                                v
       |                                                              Content Storage
       |                                                                       |
       |                                                                       v
       |                                                                Link Extractor
       |                                                                       |
       |                                                                       v
       |                                                                  URL Filter
       |                                                                       |
       |                                                                       v
       +----------------------------- (No) ---------------------------- URL Seen?  <-->  URL Storage
                                                                               |
                                                                            (Yes) vứt
```

| Khối trong sơ đồ | Lớp cài đặt |
| --- | --- |
| URL Frontier | `crawler.frontier.UrlFrontier` (+ `FrontQueues`, `BackQueues`, `DefaultPrioritizer`) |
| DNS Resolver | `crawler.DnsResolver` (LRU cache) |
| HTML Downloader | `crawler.HtmlDownloader` (jsoup + retry + chặn SSRF) |
| Content Parser | `crawler.ContentParser` |
| Language Filter | `crawler.LanguageFilter` |
| Content Seen? | `crawler.ContentSeenFilter` (SHA-256) |
| Content Storage | `crawler.ContentStorage` |
| Link Extractor | `crawler.LinkExtractor` (+ `UrlCanonicalizer`) |
| URL Filter | `crawler.UrlFilter` (+ `RobotsTxtParser`) |
| URL Seen? | `crawler.UrlSeenFilter` (Bloom filter) |
| URL Storage | `crawler.UrlStorage` (append-only, replay được) |

**Thứ tự các khối không tuỳ tiện:**

- `Content Seen?` đứng **trước** `Link Extractor` → trang trùng nội dung bị vứt
  mà không tốn công bóc liên kết.
- `URL Filter` đứng **trước** `URL Seen?` → các luật rẻ (độ sâu, domain, đuôi
  tệp) chạy trước phép tra Bloom filter.
- `Language Filter` đứng ngay sau `Content Parser` → trang ngoại ngữ không bị
  bóc liên kết, nên crawler không đi sâu vào vùng ngoại ngữ rồi vứt tiếp.

---

## 3. URL Frontier

Hai tầng hàng đợi (mô hình Mercator), `UrlFrontier` khoá toàn cục trên một
`lock`:

**Front queues — quyết định *ưu tiên*.** `DefaultPrioritizer` chia URL vào 5 mức
(0 = cao nhất): mức khởi điểm là độ sâu BFS, `-1` nếu host kết thúc bằng `.vn`,
`-1` nữa nếu URL có ≥ 5 backlink đã biết. Seed vào với điểm backlink 10, liên
kết bóc được vào với 1.

`WeightedRandomSelector` (mặc định) chọn mức theo trọng số `2^(levels-1-level)`,
tức mức 0 có xác suất gấp đôi mức 1, gấp 16 lần mức 4 — mức thấp vẫn được phục
vụ chứ không chết đói. `StrictPrioritySelector` là bản luôn lấy mức cao nhất còn
URL, dùng khi cần thứ tự tất định trong test.

**Back queues — quyết định *lịch sự*.** 128 hàng đợi, mỗi hàng gắn với đúng một
host (`Mapping Table` là `hostToQueue`), giãn cách **1000 ms/host**. `MinHeap`
sắp các hàng đợi theo thời điểm sẵn sàng, nên `poll()` luôn lấy được host đến
lượt sớm nhất mà không phải quét toàn bộ. Hàng đợi cạn thì được trả về danh sách
slot trống và gắn cho host khác.

Sức chứa mặc định 500.000 URL; vượt trần thì URL mới bị bỏ và đếm vào
`droppedDueToCapacity`.

---

## 4. Chống trùng ở hai mức

| | Chặn gì | Cấu trúc | Chi phí |
| --- | --- | --- | --- |
| `UrlSeenFilter` | tải lại cùng một **địa chỉ** | Bloom filter, FP rate 1% | ~200 URL/trang × maxPages bit (tối thiểu 200k, tối đa 50M phần tử) |
| `ContentSeenFilter` | lưu lại cùng một **nội dung** dưới hai URL | `ConcurrentHashMap` vân tay SHA-256 | 1 lần băm/trang |

Thiếu mức thứ hai thì các bản sao cùng lọt vào chỉ mục và cùng hiện trong một
trang kết quả. Vân tay được tính sau khi chuẩn hoá (hạ chữ thường, gộp khoảng
trắng), nên bản sao chỉ khác định dạng vẫn bị bắt.

`UrlSeenFilter` ghi kèm mọi URL đã gặp xuống `UrlStorage` khi
`CrawlConfig.urlStoragePath` được đặt, và `replayFromStorage()` nạp lại được.
**Lưu ý:** không dùng tệp này để nối tiếp phiên crawl — nó chứa cả hàng chục
nghìn URL còn nằm trong frontier lúc dừng, nạp lại sẽ đánh dấu chúng "đã gặp" và
khoá vĩnh viễn phần lớn không gian còn lại. Việc nối tiếp đi qua corpus (xem §8).

---

## 5. An toàn

**Chặn SSRF ở hai tầng.** `SeedUrlValidator` chặn ở tầng nhập seed;
`HtmlDownloader.ensureTargetAllowed()` chặn lại một lần nữa ngay trước khi mở kết
nối, vì URL đến từ outlink của trang đã crawl không đi qua tầng kia. Bị chặn:

- scheme khác `http`/`https`
- hostname trong danh sách chặn: `localhost`, `*.localhost`, `metadata`,
  `metadata.google.internal`, `instance-data`, `169.254.169.254`
- địa chỉ phân giải ra loopback, link-local (`169.254/16`, `fe80::/10`),
  site-local (`10/8`, `172.16/12`, `192.168/16`), any-local, multicast,
  unique-local IPv6 (`fc00::/7`), carrier-grade NAT (`100.64/10`)

URL bị chặn ném `BlockedTargetException` — là `IOException` để gọi bên ngoài bắt
chung với lỗi mạng, nhưng là lớp riêng để `download()` biết mà **không** thử lại.
`ImageDownloadService` chạy đúng bộ kiểm tra này trước mỗi lần tải ảnh.

**robots.txt.** `RobotsTxtParser` tải và cache `robots.txt` theo domain, áp luật
"longest match wins" (Allow thắng khi dài bằng nhau, theo chuẩn Google). Được gọi
trong `workerLoop` — tức *sau* khi lấy URL ra khỏi frontier, vì đây là luật đắt
(có thể phải đi mạng), các luật rẻ đã chạy từ lúc xếp hàng.

**User-Agent**: `VnSearchBot`. Timeout 10 s, tối đa 2 lần thử lại.

---

## 6. Lọc URL và lọc ngôn ngữ

`UrlFilter` loại URL theo, và **đếm riêng từng lý do**: độ sâu > maxDepth,
scheme không phải http(s), host ngoài `allowedDomains`, host bắt đầu bằng tiền tố
ngoại ngữ (`cn.`, `ja.`, `ko.`, `ru.`, `fr.`, `de.`, `th.`, …), đuôi tệp nằm
trong danh sách chặn (ảnh, css/js, pdf/office, nén, đa phương tiện), robots.txt.

`LanguageFilter` chỉ giữ tiếng Việt, tiếng Anh và "chưa xác định", quyết định
theo ba tầng bằng chứng trên 20.000 ký tự đầu (tiêu đề ghép với thân bài):

1. **Hệ chữ viết** — ≥ 10% chữ cái thuộc Han/Hiragana/Hangul/Cyrillic/Arabic/…
   thì kết luận ngay là ngoại ngữ (và ghi nhận mã ngôn ngữ tương ứng để thống kê).
2. **Ký tự riêng của tiếng Việt** (`ơ ư ă đ`) ≥ 0,5% → tiếng Việt.
3. **Từ chức năng** — ≥ 5% token là từ chức năng tiếng Việt → `vi`; ≥ 12% từ
   chức năng tiếng Anh → `en` (nới xuống 5% nếu `<html lang>` cũng khai tiếng Anh).

Dưới 40 token thì văn bản quá ngắn để kết luận, rơi về giá trị `<html lang>` /
`og:locale` do `ContentParser` bóc được.

---

## 7. Event bus và ba Modular Service

Sau khi lưu trang, `CrawlerService` **phát một `PageEvent` lên bus rồi quên đi**.
Ba service phía sau tự lấy phần của mình:

| Service | Nhận | Làm gì |
| --- | --- | --- |
| `UrlExtractorService` | `PageEvent` | Bóc liên kết → `UrlFilter` → `UrlSeenFilter` → phát `DiscoveredUrl` về frontier; phát `OutlinksExtracted` để ghi đồ thị liên kết vào tài liệu |
| `ImageDownloadService` | `PageEvent` | Bóc `<img>` (`data-src`/`data-original`/`src`), tối đa 50 ảnh/trang, phát `ImageFound`; tuỳ chọn tải thật (mặc định **tắt**, trần 5 MB/ảnh) |
| `CrawlAnalyticsService` | `PageEvent`, `ImageFound` | Đẩy số liệu vào Micrometer: kích thước HTML, độ dài thân bài, số ảnh/trang, số trang theo ngôn ngữ, số host phân biệt (trần 10.000), độ sâu lớn nhất |

Hai bản cài của bus:

- **`InProcessCrawlEventBus`** (mặc định) — publish là lời gọi hàm đồng bộ.
  `CrawlerService()` tự dựng bus và tự đăng ký ba service, nên
  `MultiDomainCrawlRunner` và toàn bộ test chạy được mà không cần hạ tầng gì.
- **`KafkaCrawlEventBus`** — bốn topic (`pages`, `urls`, `outlinks`, `images`),
  **khoá phân hoạch là `host`** để mọi trang cùng host rơi vào cùng partition.
  Bật bằng `app.crawler.bus=kafka`. Khi bus được tiêm từ ngoài, `CrawlerService`
  **không** đăng ký service cục bộ — chúng chạy ở tiến trình khác.

`ImageStore` giữ **một ảnh tốt nhất mỗi trang**: `ImageQuality` xếp ảnh theo bậc
(có kích thước ≥ 200px > không rõ kích thước > nhỏ > trang trí), rồi theo chiều
rộng ước lượng (thuộc tính `width`, tham số `?w=`/`?width=`, hoặc `_800x600_`
trong đường dẫn), rồi ưu tiên ảnh có `alt`. Ảnh `svg/gif/ico/bmp` và ảnh có
`thumb|icon|logo|avatar|sprite|banner|favicon|1x1|…` trong đường dẫn bị xếp bậc
thấp nhất.

---

## 8. Nối tiếp corpus và checkpoint

`crawl(seeds, config, previousDocuments)` chạy **nối tiếp**: giữ nguyên tài liệu
cũ, không tải lại chúng, và đi tiếp từ chính outlinks của chúng. Corpus cũ được
đưa trở lại đúng ba khối, mỗi khối một lý do:

- `ContentStorage` — để tệp ghi ra cuối phiên là corpus **tổng**, không phải chỉ
  phần mới (thiếu bước này thì "nối tiếp" thực chất là ghi đè).
- `UrlSeenFilter` — chặn tải lại trang đã có.
- `ContentSeenFilter` — giữ vân tay cũ, để một trang cũ xuất hiện lại dưới URL
  khác không thành bản sao thứ hai.

Độ sâu của mọi liên kết cũ đặt lại về 1: độ sâu là thuộc tính của *đường đi*
trong một phiên, không phải của trang. Hệ quả cần biết — mỗi lần chạy nối tiếp,
corpus lan rộng thêm `maxDepth` tầng nữa.

`CheckpointCrawlListener` ghi corpus + kho ảnh ra đúng tệp đầu ra sau mỗi 250
trang (và không sớm hơn mức tăng 25% so với checkpoint trước), ghi trên một
thread daemon riêng, bỏ qua lượt ghi nếu lượt trước chưa xong. `ContentStorage`
ghi qua tệp `.tmp` rồi `ATOMIC_MOVE`, nên checkpoint không bao giờ để lại tệp
JSON dở dang.

---

## 9. Cấu hình

`CrawlConfig` là bất biến, dựng bằng builder, mọi phép kiểm tra tập trung trong
`build()`:

| Trường | Mặc định | Ý nghĩa |
| --- | --- | --- |
| `maxDepth` | 3 | Độ sâu BFS tối đa |
| `maxPages` | 100 | Trần số trang **lưu được** (không phải số trang tải) |
| `threadCount` | 4 | Số worker thread |
| `allowedDomains` | rỗng = không giới hạn | Khớp cả subdomain (`endsWith("." + d)`) |
| `excludedHostPrefixes` | rỗng | Tiền tố host bị loại, ví dụ `UrlFilter.NON_VI_EN_HOST_PREFIXES` |
| `maxDurationMinutes` | 60 | Trần thời gian cả phiên |
| `urlStoragePath` | `null` = tắt | Tệp ghi mọi URL đã gặp |

`MultiDomainCrawlRunner` dựng sẵn cấu hình cho 19 seed (11 tiếng Việt + 8 tiếng
Anh: vnexpress, tuoitre, dantri, thanhnien, vietnamnet, nhandan, baochinhphu,
vietnamplus, vietnamnews, vov, vir…), `allowedDomains` suy ra từ seed sau khi bỏ
nhãn ngôn ngữ (`e.`/`en.`/`www.`), `threadCount = min(32, 2 × số host)`,
`maxDurationMinutes = 180`.

---

## 10. Đầu ra

`data/crawled-documents.json` — mảng `WebDocument`:

```json
[
  {
    "docId": 0,
    "url": "https://vnexpress.net/bai-viet",
    "title": "…",
    "metaDescription": "…",
    "bodyText": "…",
    "outlinks": ["https://vnexpress.net/bai-khac", "…"],
    "crawledAt": "2026-08-21T09:57:03.412Z",
    "language": "vi"
  }
]
```

`docId` được cấp **sau** khi lưu thành công, từ một bộ đếm riêng, nên dãy luôn
đặc `0..n-1` kể cả khi nhiều worker cùng về đích hoặc phiên nối tiếp cấp tiếp từ
mốc corpus cũ.

`data/crawled-documents.images.json` — mảng `ImageFound` (`pageUrl`, `host`,
`imageUrl`, `altText`, `declaredWidth/Height`, `sizeBytes`, `contentHash`);
đường dẫn suy ra từ tệp corpus bằng `ImageStorage.pathFor()`.

`bodyText` đã loại `script, style, noscript, nav, footer, header, iframe, svg`.
URL trong `outlinks` đã chuẩn hoá: bỏ fragment, hạ chữ thường scheme/host, bỏ
cổng mặc định, bỏ dấu `/` thừa cuối đường dẫn, giữ nguyên query.

---

## 11. Số liệu cuối phiên

`MultiDomainCrawlRunner` in báo cáo theo **từng khối**, lấy từ bộ đếm của chính
các lớp đó:

```
=== THONG KE THEO TUNG KHOI ===
DNS Resolver   : n host trong cache, ty le trung x%, n host chet bi loai som
HTML Downloader: tai n trang, n lan thu lai, n that bai
Language Filter: GIU n vi + n en + n chua ro, VUT n ngoai ngu  (zh n | ja n | …)
Content Seen?  : n noi dung phan biet, VUT n ban trung, n trang than bai rong
URL Filter     : nhan n, loai n  (domain n | duoi tep n | do sau n | scheme n | robots n)
URL Seen?      : n URL phan biet, bo loc n bit (x KB), n ham bam
URL Storage    : …
```

Rồi thống kê corpus: tổng trang, thời gian, thông lượng trang/giây, tổng outlink,
phân bố theo domain và theo ngôn ngữ, số cạnh đồ thị (nội bộ / chéo domain), tỉ
lệ thưa `nnz/n²`, và cảnh báo domain nào không crawl được trang nào.

`crawl-stats.bat` đọc lại tệp JSON và in sâu hơn: phân vị số outlink mỗi trang,
tỉ lệ URL trùng lặp, chặn trên của hàng đợi còn lại, hệ số nhân của frontier
(> 1 nghĩa là hàng đợi không bao giờ cạn), và báo cáo kho ảnh.

---

## 12. Test

```bash
cd search-engine
./mvnw test
```

272 test, tất cả đều pass. Phạm vi: toàn bộ `com/vnsearch/crawler/**` cộng ba bộ
test cấu trúc dữ liệu mà crawler thật sự dùng (`BloomFilterTest`, `LRUCacheTest`,
`MinHeapTest`).

Đáng chú ý: `SsrfProtectionTest` — tệp `SsrProtectionTest.java` — (chặn địa chỉ
nội bộ ở cả tầng seed lẫn tầng tải trang),
`RobotsTxtParserTest` (luật longest-match), `BackQueuesTest` / `UrlFrontierTest`
(lịch sự theo host và ràng buộc một host một hàng đợi), `LanguageFilterTest`,
`CrawlerServiceBusWiringTest` (đúng ba service được đăng ký ở chế độ in-process
và **không** đăng ký ở chế độ bus ngoài).

`KafkaCrawlBusIT` bị loại khỏi vòng test thường (`<testExcludes>`) vì cần Docker
qua Testcontainers.

---
