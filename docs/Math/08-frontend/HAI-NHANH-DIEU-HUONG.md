# Hai nhánh điều hướng — gõ gì vào ô địa chỉ thì chuyện gì xảy ra

**File nguồn:** `renderer/src/components/AddressBar.tsx`, `renderer/src/store/tabStore.ts`, `preload/index.ts`, `main/ipcHandler.ts`, `main/tabManager.ts`
**Việc nó làm:** Phân loại thứ người dùng gõ, rồi rẽ về **một trong hai hệ thống hoàn toàn khác nhau**.

---

## 📌 Hiểu trong 30 giây

Ô địa chỉ (omnibox) trông như một ô nhập duy nhất, nhưng phía sau nó là **hai đường đi không giao nhau**:

```
                        người dùng gõ + Enter
                                 │
                    ┌────────────┴────────────┐
                    │   looksLikeUrl(value)   │   ← AddressBar.tsx:16
                    └────────────┬────────────┘
                  true │                     │ false
                       ▼                     ▼
            ┌──────────────────┐   ┌──────────────────────┐
            │ NHÁNH 1          │   │ NHÁNH 2              │
            │ Chromium tải     │   │ Gọi API search engine│
            │ trang web thật   │   │ của chính mình       │
            │ (không API nào)  │   │ (localhost:8080)     │
            └──────────────────┘   └──────────────────────┘
```

**Điểm hay gây nhầm:** gõ `youtube.com` thì YouTube hiện ra đầy đủ, video chạy được — nhưng **search engine của dự án không hề tham gia**. Đó là Chromium nhúng sẵn trong Electron làm việc. Search engine chỉ vào cuộc ở nhánh 2.

---

## 1. `looksLikeUrl()` — bộ phân loại quyết định tất cả

```ts
function looksLikeUrl(text: string): boolean {
  const trimmed = text.trim()
  if (/^https?:\/\//i.test(trimmed)) {
    return true
  }
  return !trimmed.includes(' ') && /\.[a-z]{2,}(\/.*)?$/i.test(trimmed)
}
```

Hai luật, theo thứ tự:

| Luật | Ý nghĩa | Ví dụ `true` |
|---|---|---|
| Có tiền tố `http://` hoặc `https://` | Người dùng nói rõ đây là URL | `https://vnexpress.net` |
| **Không có dấu cách** VÀ kết thúc bằng `.` + ≥2 chữ cái (kèm đường dẫn tuỳ ý) | Đoán là tên miền | `youtube.com`, `github.com/topics` |

Đây là một **heuristic**, không phải bộ phân tích URL đúng chuẩn — và nó phải như vậy. Trình duyệt nào cũng đoán, vì `mèo con dễ thương` và `youtube.com` đều là chuỗi ký tự hợp lệ; không có cách nào biết chắc ý định người dùng.

**Kết quả thật khi chạy thử từng ca:**

| Người dùng gõ | Kết quả | Nhận xét |
|---|---|---|
| `youtube.com` | `true` | đúng ý |
| `github.com/topics` | `true` | đúng ý — nhóm `(\/.*)?` cho phép đường dẫn |
| `mèo con dễ thương` | `false` | đúng ý — có dấu cách |
| `3.14` | `false` | đúng — sau dấu chấm phải là **chữ cái** |
| `chó.mèo` | `false` | đúng — `è` không thuộc `[a-z]`, nên đuôi `mèo` không khớp |
| `file.pdf` | ⚠️ `true` | **sai** — người dùng tìm tên file, bị đem đi mở `https://file.pdf` |
| `tôi.là.ai` | ⚠️ `true` | **sai** — chỉ cần *đoạn cuối* (`ai`) là chữ ASCII là đủ khớp |
| `localhost:3000` | ⚠️ `false` | **sai** — không có dấu chấm nên bị đem đi tìm kiếm |
| `192.168.1.1:8080` | ⚠️ `false` | **sai** — đuôi `1:8080` không phải chữ cái |

Bốn ca cuối là giới hạn thật của heuristic này, không phải lỗi cài đặt. §7 nói cách trình duyệt thật xử lý.

---

## 2. Nhánh 1 — gõ `youtube.com` → mở web thật

```
AddressBar.tsx:74   looksLikeUrl('youtube.com') → true
                    (không có dấu cách + kết thúc bằng .com)
        ↓
AddressBar.tsx:76   navigate('https://youtube.com')
        ↓
tabStore.ts:58      window.browser.navigate(id, url)     ← cầu preload
        ↓
preload/index.ts:12 ipcRenderer.invoke('browser:navigate', id, url)   ← IPC
        ↓
ipcHandler.ts:13    tabManager.navigate(id, url)         ← main process
        ↓
tabManager.ts:207   view.webContents.loadURL(target)     ← Chromium tải trang
```

`view` ở đây là một `WebContentsView` (tabManager.ts:233) — đúng nghĩa **một cái tab Chrome thu nhỏ**. Nó tự tải HTML/CSS/JS của YouTube, chạy video, xử lý đăng nhập, mọi thứ. App chỉ ra lệnh *"load URL này"* chứ không đụng gì tới nội dung.

**Vì sao phải đi vòng qua 3 tiến trình.** Renderer (React) chạy trong sandbox, `contextIsolation: true` và `nodeIntegration: false` (tabManager.ts:62–63), nên nó **không được** gọi thẳng API Electron. Preload là lớp duy nhất nhìn thấy cả hai phía; nó phơi ra một mặt tiền hẹp (`window.browser`) rồi chuyển tiếp qua IPC. Đây là mẫu **Facade + ranh giới an ninh**, không phải thủ tục thừa: nếu renderer bị XSS từ một trang web độc, kẻ tấn công cũng chỉ gọi được đúng những hàm preload cho phép.

Bù `https://` diễn ra **hai lần**, cả hai đều cần:

```ts
// AddressBar.tsx:76 — để URL hiển thị trong ô địa chỉ là URL đầy đủ
navigate(/^https?:\/\//i.test(value) ? value : `https://${value}`)

// tabManager.ts:203 — chốt chặn cuối, vì navigate() còn được gọi từ chỗ khác
const target = /^[a-z]+:\/\//i.test(url) ? url : `https://${url}`
```

---

## 3. Nhánh 2 — gõ `mèo con dễ thương` → mới dùng API

```
AddressBar.tsx:78   navigate(HOME_URL) + runSearch(value)
        ↓
searchViewStore.ts:39  → searchApi.ts:61 search(query)
        ↓
searchApi.ts:67     fetch('http://localhost:8080/api/search?q=...&page=0')
        ↓
Backend Java trả JSON → React render <SearchResultList />
```

Đây mới là chỗ **search engine của dự án** hoạt động: crawler → chỉ mục ngược → BM25 → PageRank, toàn bộ nội dung của các nhóm 1–7 trong thư mục này.

Chú ý thứ tự hai lệnh ở AddressBar.tsx:78–79 — **không hoán đổi được**:

```ts
navigate(HOME_URL)   // 1. huỷ WebContentsView, trả màn hình về cho React
runSearch(value)     // 2. gọi API, đổ kết quả vào store
```

Nếu gọi ngược lại, `SearchResultList` sẽ render **phía sau** trang web đang mở — mà `WebContentsView` là lớp native nằm đè lên (xem §5), nên người dùng không thấy gì.

**Backend chết thì không lỗi, mà rơi xuống dữ liệu giả.** `searchApi.ts:80` có tầng dự phòng `mockSearch()` chạy trên `MOCK_CORPUS` gồm ~8 tài liệu cứng. Cờ `mock: true` đi kèm kết quả để giao diện nói rõ cho người dùng biết đang xem số liệu giả. Kèm theo là bộ nhớ đệm trạng thái (`backendDown` + `RECHECK_MS = 15_000`) để không phải chờ timeout 1,5 giây ở **mỗi** lần gõ khi backend đang tắt.

---

## 4. `HOME_URL` — một URL giả để hai nhánh chung sống

```ts
export const HOME_URL = 'vnsearch://home'    // tabStore.ts:5 và tabManager.ts:9
```

Chuỗi này **không phải giao thức thật**, Chromium không hiểu nó. Nó là một **giá trị canh gác** (sentinel) được bắt riêng trước khi tới `loadURL`:

```ts
// tabManager.ts:194
if (url === HOME_URL) {
  this.destroyView(entry)        // ← HUỶ WebContentsView đi
  entry.state = { ...entry.state, url: HOME_URL, title: 'Tab mới', loading: false }
  ...
  return                         // ← không bao giờ chạm tới loadURL
}
```

Nhờ vậy **trang chủ và trang kết quả tìm kiếm là React**, còn **trang web ngoài là Chromium** — hai hệ vẽ khác nhau, cùng nằm trong một tab. `App.tsx:19` đọc đúng cờ này để quyết định vẽ gì:

```ts
const showInternalContent = useTabStore((s) => {
  const tab = s.tabs.find((t) => t.id === s.activeTabId)
  return !tab || tab.url === HOME_URL
})
```

> ⚠️ Comment ở tabStore.ts:4 cảnh báo: hằng này **lặp lại ở hai nơi** (renderer và main) và phải luôn khớp nhau. Đây là một bất biến không được trình biên dịch bảo vệ — đổi một chỗ mà quên chỗ kia thì trang chủ sẽ cố tải `vnsearch://home` như URL thật và hỏng.

---

## 5. Hệ quả kiến trúc: `WebContentsView` là lớp native, không tuân theo CSS

Đây là điều bất ngờ nhất khi làm trình duyệt bằng Electron, và nó giải thích ba đoạn code trông thừa thãi.

Cấu trúc cửa sổ **không** phải là React chứa trang web, mà là:

```
BrowserWindow  ← chỉ còn là cái khung rỗng (tabManager.ts:71)
├── chromeView       (WebContentsView)  ← toàn bộ giao diện React
└── tab view         (WebContentsView)  ← trang web, ĐÈ LÊN chromeView
```

Hai `WebContentsView` là hai **lớp hệ điều hành** xếp chồng. `z-index` của CSS chỉ có tác dụng *bên trong* một lớp — không lớp nào "nhìn thấy" lớp kia. Ba hệ quả:

| Vấn đề | Cách xử lý | Vị trí |
|---|---|---|
| Trang web che mất giao diện | Đặt `y: CHROME_HEIGHT` (= 122 px = tabbar 40 + toolbar 44 + bmbar 38) để chừa chỗ | tabManager.ts:97–105 |
| Mở SidePanel thì trang web phải co lại | React báo bề rộng panel xuống main qua `setPanelWidth` | App.tsx:34, tabManager.ts:107 |
| Menu/dropdown React bị trang web che | **Ẩn hẳn** tab view khi có overlay | App.tsx:38, tabManager.ts:116 |

Cách thứ ba được chính comment trong code thừa nhận là giải pháp tạm:

> *"TAM THOI: tab view luon nam DE len tren chromeView, nen menu/dropdown do React ve se bi trang web che mat. Cach re nhat: an tab view khi co overlay."*

**Cái giá phải trả:** mỗi lần mở menu, trang web biến mất rồi hiện lại — nhìn thấy được. Cách đúng là dùng `BrowserWindow` con trong suốt cho overlay, phức tạp hơn nhiều.

Và vì `CHROME_HEIGHT = 122` là hằng số **chôn cứng** trong main process, mọi thay đổi chiều cao ở CSS phía React (`.tabbar`, `.toolbar`, `.bmbar` trong `main.css`) đều phải sửa kèm con số này — nếu không, trang web sẽ bị lệch hoặc bị che một dải.

---

## 6. Bảng đối chiếu hai nhánh

| | Nhánh 1 — URL | Nhánh 2 — từ khoá |
|---|---|---|
| Điều kiện | `looksLikeUrl()` → `true` | `looksLikeUrl()` → `false` |
| Ai vẽ nội dung | Chromium (`WebContentsView`) | React (`SearchResultList`) |
| Có gọi backend Java không | **Không** | Có — `localhost:8080/api/search` |
| Search engine của dự án có tham gia không | **Không** | Có — toàn bộ pipeline nhóm 1–7 |
| Tab có `view` không | Có | Không (`destroyView` ở tabManager.ts:196) |
| Back/Forward do ai quản | `webContents.navigationHistory` của Chromium | [Stack.md](Stack.md) — hai ngăn xếp tự cài |
| Backend chết thì sao | Không ảnh hưởng | Rơi xuống `mockSearch()`, cờ `mock: true` |

---

## 7. Hạn chế đã biết

1. **`looksLikeUrl` đoán sai ở bốn ca đã liệt kê ở §1** — `file.pdf`, `tôi.là.ai` bị mở nhầm; `localhost:3000`, `192.168.1.1:8080` bị tìm nhầm. Trình duyệt thật giải quyết bằng cách **thử phân giải DNS song song** với việc tìm kiếm rồi mới quyết, cộng danh sách TLD hợp lệ (IANA) thay vì regex `[a-z]{2,}`.
2. **Không có nhánh thứ ba cho "tìm bằng Google".** Mọi từ khoá đều đi vào search engine của dự án, mà corpus chỉ có 5.011 trang — người dùng gõ gì lạ là không có kết quả.
3. **`HOME_URL` lặp ở hai tiến trình**, không có kiểm tra tự động nào bắt được khi lệch (§4).
4. **`CHROME_HEIGHT` lặp giữa CSS và main process**, cùng loại vấn đề (§5).
5. **Ẩn/hiện tab view khi mở menu** gây nháy hình (§5).

---

## 🔗 Liên quan

| Tài liệu | Nội dung |
|---|---|
| [Stack](Stack.md) | Back/Forward — hai ngăn xếp và bất biến của chúng |
| [BookmarkTrie](BookmarkTrie.md) | Cây tiền tố cho gợi ý bookmark trong cùng ô địa chỉ này |
| [Trie](../06-datastructures/Trie.md) | Bản Java, dùng cho `/api/suggest` mà ô địa chỉ gọi khi gõ ≥ 2 ký tự |
| [CandidateResolver](../04-query/CandidateResolver.md) | Nhánh 2 đi tiếp vào đâu ở phía backend |
