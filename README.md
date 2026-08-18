# Browser app like CocCoc and Vietnamese search engine

A Vietnamese search engine built from scratch — crawler, inverted index, ranking,
and a mini browser to query it.

Every core data structure and algorithm is **hand-written**, with no off-the-shelf
search library: inverted index, VByte compression, PageRank, Trie, Bloom filter,
MinHeap, and a Vietnamese word segmenter.

```
┌──────────────┐    ┌──────────────┐    ┌──────────────┐    ┌──────────────┐
│   Crawler    │───▶│    Index     │───▶│   Ranking   │───▶│   REST API  │
│              │    │              │    │              │    │              │
│ UrlFrontier  │    │ InvertedIndex│    │ TF-IDF/BM25  │    │ /api/search  │
│ BloomFilter  │    │ VByte + delta│    │ PageRank     │    │ /api/suggest │
│ robots.txt   │    │ VN segmenter │    │ MinHeap top-K│    │ /api/admin   │
└──────────────┘    └──────────────┘    └──────────────┘    └──────┬───────┘
                                                                    │
                                                            ┌───────▼───────┐
                                                            │  browser-app  │
                                                            │  (Electron)   │
                                                            └───────────────┘

```

---

## Quick start — Docker

Requires Docker Desktop.

```bash
# 1. Create your config file from the template
cp .env.example .env

# 2. Generate an admin key and paste it into .env
openssl rand -hex 32
#   PowerShell: -join ((1..64) | % { '{0:x}' -f (Get-Random -Max 16) })

# 3. Run
docker compose up -d --build
```

The backend serves on `http://localhost:8080`. First boot takes a few tens of
seconds to build the index — follow it with `docker compose logs -f backend`.

```bash
curl "http://localhost:8080/api/health"
curl "http://localhost:8080/api/search?q=máy+tính&size=3"
```

> **If `docker compose up` stops immediately with "Thieu ADMIN_API_KEY"** — that
> is deliberate, not a bug. Step 2 above has not been done.
> See [Why the admin key is mandatory](#why-the-admin-key-is-mandatory).

### Optional profiles

The default stack is deliberately the lightest thing that still works. Two
opt-in profiles add the distributed crawl pipeline and the observability chain:

```bash
# + Kafka, kafka-ui, a separate crawler-worker process   (~3 GB RAM)
docker compose --profile kafka up -d --build

# + Prometheus, Grafana, Alertmanager, kafka-exporter    (~4 GB RAM)
docker compose --profile kafka --profile monitoring up -d --build

# + football-service (Go), feeding the browser's Sports panel   (~30 MB RAM)
docker compose --profile football up -d --build
```

| Address | What you get |
|---|---|
| <http://localhost:8081> | kafka-ui — topics, partitions, consumer lag, dead-letter messages |
| <http://localhost:3000> | Grafana (`admin`/`admin`), dashboard pre-provisioned |
| <http://localhost:9090/alerts> | Prometheus — the 7 alert rules and their state |
| <http://localhost:9093> | Alertmanager |
| <http://localhost:8090/api/v1/status> | football-service — daily API budget left |

Details: [`docs/DEVOPS.md`](docs/DEVOPS.md).

---

## Running without Docker

Requires JDK 17+ and Node.js 22+.

### Backend

```bash
run-backend.bat             # Windows

# or, by hand:
export ADMIN_API_KEY=$(openssl rand -hex 32)          # Linux/macOS
$env:ADMIN_API_KEY = "..."                             # PowerShell
cd search-engine
./mvnw spring-boot:run
```

`run-backend.bat` reads `ADMIN_API_KEY` from `.env` (generating and saving one
if absent), checks port 8080, sets a 6 GB heap, and warns when `data/index.json`
is older than the crawled corpus. Flags: `--postgres`, `--kafka`, `--bm25`,
`--help`.

No database required: the app falls back to the sample corpus shipped with the
repo (`data/seed-documents.json`), so a fresh clone runs as-is.

### Frontend

```bash
run-frontend.bat            # Windows
# or: cd browser-app && npm install && npm run dev
```

### Crawling your own corpus

```bash
run-crawl.bat 5000 3        # 5,000 pages, depth 3
```

---

## Kubernetes

A three-node [kind](https://kind.sigs.k8s.io/) cluster, ingress, and the full
stack in one command:

```bash
bash deploy/kind/up.sh
# then add to your hosts file:  127.0.0.1 vnsearch.local
curl http://vnsearch.local/api/health
```

Manifests use Kustomize with a shared base and two overlays:

| | `overlays/dev` | `overlays/prod` |
|---|---|---|
| Replicas | 1 | 3, spread across nodes |
| Autoscaling | off (no metrics-server in kind) | HPA, 2–6 pods at 70% CPU |
| Secrets | placeholder file in Git | created out-of-band, never committed |
| Image | local build, `kind load` | pinned tag from GHCR |
| Scorer | `tfidf` | `bm25` |

The backend runs as non-root with a read-only root filesystem under a
`restricted` Pod Security namespace, has startup/readiness/liveness probes, a
PodDisruptionBudget, and a NetworkPolicy restricting Postgres to backend pods
only.

```bash
kubectl apply -k deploy/k8s/overlays/dev     # or overlays/prod
bash deploy/kind/down.sh                     # tear the cluster down
```

---

## API

23 endpoints. The middle column is the *role* required, not the mechanism.

| Endpoint | Access | Description |
|---|:---:|---|
| `GET /api/search?q=&page=&size=` | — | Search |
| `GET /api/suggest?prefix=&limit=` | — | Prefix suggestions (Trie). Note: `prefix`, **not** `q` |
| `GET /api/images?q=&page=&size=` | — | Image search, backed by `ImageStore` |
| `GET /api/feed?seed=&page=&size=` | — | Browse the index without a query. Same `seed` ⇒ same order, so pages join up |
| `GET /api/health` | — | Liveness. Returns `503` when the index is empty |
| `GET /actuator/prometheus` | — | Prometheus metrics |
| `POST /api/events` | — | Write side of usage analytics — deliberately open |
| `POST /api/auth/register` | — | Always creates a `USER`; there is no way to self-assign `ADMIN` |
| `POST /api/auth/login` | — | Returns an opaque 256-bit token, valid 12 hours |
| `POST /api/auth/logout` | — | Revokes the token immediately; open so an *expired* token can still log out |
| `GET /api/auth/me` | 🔑 | Who am I |
| `POST /api/auth/password` | 🔑 | Requires the current password even with a valid token |
| `POST /api/auth/logout-all` | 🔑 | Revokes every session of this account |
| `POST /api/admin/crawl` | 👑 | Start a crawl job |
| `GET /api/admin/crawl/{id}/status` | 👑 | Crawl job status |
| `POST /api/admin/reindex` | 👑 | Rebuild the index |
| `GET /api/admin/stats` | 👑 | Detailed statistics |
| `GET /api/admin/analytics` | 👑 | One JSON with traffic, crawl, index and account figures |
| `POST /api/admin/analytics/reset` | 👑 | Clears traffic figures only — never touches the index |
| `GET /api/admin/users` | 👑 | Never includes password hashes |
| `POST /api/admin/users/{name}/role` | 👑 | Also closes every session of that user |
| `POST /api/admin/users/{name}/disable` · `/enable` | 👑 | Keeps the data, blocks login |
| `DELETE /api/admin/users/{name}` | 👑 | `400` if you try to delete yourself |

🔑 = signed in · 👑 = `ADMIN`

**Two ways to authenticate, one authorisation table.** Tools use a static
`X-API-Key` (no identity, never expires, always full `ADMIN`); people use an
account and get `Authorization: Bearer` (identity, 12-hour expiry, revocable
instantly). Both feed the *same* role check in `SecurityConfig` — adding OAuth
later means adding a filter, not editing the table.

```bash
curl -H "X-API-Key: $ADMIN_API_KEY" http://localhost:8080/api/admin/stats
curl -H "Authorization: Bearer $TOKEN"  http://localhost:8080/api/auth/me
```

The first admin account is created at boot from `BOOTSTRAP_ADMIN_PASSWORD` —
there is no default password, on purpose. See
[`docs/CONFIGURATION.md`](docs/CONFIGURATION.md) §3b.

Full examples: [`docs/api-examples.http`](docs/api-examples.http)

---

## Why the admin key is mandatory

`POST /api/admin/crawl` makes the server **fetch a URL chosen by the caller**
and put the contents into an index that `GET /api/search` reads publicly. Leaving
it open is a complete SSRF vulnerability with an exfiltration channel attached —
on a cloud VM, a request to `169.254.169.254` returns temporary IAM credentials.

So the app **deliberately refuses to start** without a key. The alternative —
generating a key and printing it to the log — produces a system that *looks*
healthy while nobody knows the key. Fail loudly rather than fail silently.

Four independent layers, each blocking something different:

| Layer | Blocks | Implemented in |
|---|---|---|
| API key (constant-time comparison) | Strangers | `ApiKeyAuthFilter` |
| Private IP ranges blocked **after DNS resolution**, on every fetch and every redirect hop | URLs pointing into the internal network, even with a valid key | `SeedUrlValidator` + `HtmlDownloader` |
| Caps on `maxPages` / `maxDepth` | A single valid request exhausting resources | `AdminController` |
| Rate limiting (token bucket) | Correct calls arriving too fast | `RateLimitFilter` |

---

## Repository layout

```
search-engine/          Spring Boot backend (Java 17)
  src/main/java/com/vnsearch/
    crawler/            Fetching, URL filtering, two-tier frontier
    index/              Inverted index, VByte compression, VN segmenter
    query/              Query parsing, posting-list merging
    ranking/            TF-IDF, BM25, PageRank, snippet generation
    datastructure/      Trie, BloomFilter, MinHeap, LRUCache, SparseMatrix
    eval/               Search quality harness
browser-app/            Mini browser (Electron + React + TypeScript)


```

