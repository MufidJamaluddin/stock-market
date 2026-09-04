# Stock Market Dashboard (Reactive Spring)

A fully reactive stock market dashboard and search/detail application built with
Spring WebFlux, demonstrating: Spring IoC, Java Streams, advanced native SQL,
containerization & microservices, Kafka stream processing, Redis caching
strategies, and Elasticsearch as a non-relational search store.

## Architecture

```
                     ┌────────────────────┐
                     │  MarketDataGenerator │  (Java Streams, @Scheduled)
                     │  simulates ticks      │
                     └──────────┬─────────┘
                                │ publishes JSON
                                ▼
                          ┌──────────┐
                          │  Kafka   │  topic: stock-price-ticks
                          └────┬─────┘
                               │ Reactor Kafka (KafkaReceiver)
                               ▼
                   ┌──────────────────────────┐
                   │ StockPriceConsumerService │
                   └─────┬───────┬──────┬──────┘
                         │       │      │
             ┌───────────┘   ┌───┘      └──────────┐
             ▼               ▼                     ▼
     ┌───────────────┐ ┌───────────┐      ┌──────────────────┐
     │ PostgreSQL     │ │  Redis    │      │  Elasticsearch    │
     │ (R2DBC)        │ │ (write-   │      │  (search index,    │
     │ history+latest │ │  through) │      │  refreshed live)   │
     └───────┬────────┘ └─────┬─────┘      └─────────┬──────────┘
             │                │                        │
             └──────┬─────────┴───────────┬────────────┘
                    ▼                     ▼
            REST controllers (WebFlux)   SSE stream
                    │                     │
                    └──────────┬──────────┘
                               ▼
                    Simple static UI (HTML/JS)
```

## Tech mapping to requirements

| Requirement                         | Where it lives |
|--------------------------------------|----------------|
| Spring IoC                          | Constructor injection throughout (`@RequiredArgsConstructor`), `@Configuration` beans in `config/` |
| Java Stream                         | `MarketDataGeneratorService` (tick generation pipeline), `StockService#toDetailDto`, `SearchIndexInitializer` |
| Advanced native SQL query           | `StockNativeRepositoryImpl` - CTE + window functions (`FIRST_VALUE`/`LAST_VALUE`/`MAX`/`MIN`/`SUM`) for OHLC candles, ranked gainers/losers |
| Containerization & microservices    | `Dockerfile`, `docker-compose.yml`, `k8s/*.yaml` (each store is its own deployable service) |
| Kafka & stream-based application    | `KafkaConfig`, `MarketDataGeneratorService` (producer), `StockPriceConsumerService` (consumer), both via Reactor Kafka |
| Redis, caching strategy & data grid | `CacheService` - cache-aside for detail/search, write-through for live quotes (`RedisConfig`) |
| Elastic & other non-relational DB   | `StockSearchDocument`, `StockSearchRepository`, `SearchService`, `SearchIndexInitializer` |
| Flyway SQL migration                | `src/main/resources/db/migration/V1__*.sql` .. `V3__*.sql` |
| Raw Query to Class / Repository pattern | `StockNativeRepository` (interface) + `StockNativeRepositoryImpl` (raw SQL mapped to `StockDto`/`OhlcBarDto`) alongside standard `R2dbcRepository`s |
| Simple UI                           | `src/main/resources/static/{index.html,app.js,style.css}` |

## Run locally with Docker Compose

```bash
cd stock-dashboard
docker compose up --build
```

Then open **http://localhost:8080**. The dashboard live-updates via
Server-Sent Events, backed by the Kafka tick generator running every 2s
(configurable via `app.market.generator.interval-ms`).

Services exposed:
- App: `localhost:8080`
- Postgres: `localhost:5432` (db `stockdb`, user/pass `stock`/`stock`)
- Redis: `localhost:6379`
- Elasticsearch: `localhost:9200`
- Kafka: `localhost:9092`

## Run on Kubernetes

```bash
cd stock-dashboard
docker build -t stock-dashboard:latest .
# make the image available to your cluster (kind load / minikube image load / push to a registry)

kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/
```

Manifests are numbered so `kubectl apply -f k8s/` applies them in a sane order
(config → stateful stores → app → networking/scaling). The app Deployment
starts with 2 replicas and an HPA (`09-hpa.yaml`) scales 2-6 on CPU/memory.

Check status:
```bash
kubectl -n stock-dashboard get pods
kubectl -n stock-dashboard logs -f deploy/stock-dashboard-app
```

Add `127.0.0.1 stock-dashboard.local` to your hosts file (or port-forward the
service directly) to use the Ingress, or simply:
```bash
kubectl -n stock-dashboard port-forward svc/stock-dashboard-app 8080:80
```

## Key REST endpoints

| Method | Path                              | Description |
|--------|------------------------------------|--------------|
| GET    | `/api/stocks`                     | Full dashboard listing with computed change % |
| GET    | `/api/stocks/gainers?limit=5`     | Top gainers (native SQL ranking) |
| GET    | `/api/stocks/losers?limit=5`      | Top losers |
| GET    | `/api/stocks/sector/{sector}`     | Sector leaders |
| GET    | `/api/stocks/{symbol}`            | Stock detail (cache-aside, includes recent ticks) |
| GET    | `/api/stocks/{symbol}/ohlc?bucket=minute&limit=50` | OHLC candlestick bars (window-function query) |
| GET    | `/api/search?q=appl`              | Elasticsearch-backed fuzzy search |
| GET    | `/api/stocks/stream`              | SSE stream of all live ticks |
| GET    | `/api/stocks/{symbol}/stream`     | SSE stream filtered to one symbol |

## Notes & caveats

- This was built and reviewed in an environment without access to Maven
  Central, so it has **not** been compiled/build-tested here. The code was
  written and manually reviewed carefully against the actual Spring Boot 3.3 /
  Spring Data R2DBC / Reactor Kafka / Spring Data Elasticsearch 5.x APIs, but
  you should run `mvn clean package` locally / in CI before deploying, and
  file an issue-style note back if anything doesn't compile so it can be
  patched.
- Flyway runs migrations via a small dedicated JDBC connection pool (2
  connections) at startup only; all runtime queries use the fully reactive
  R2DBC path.
- The market data generator can be disabled (`MARKET_GENERATOR_ENABLED=false`)
  if you want to drive ticks from an external/real feed into the same Kafka
  topic instead.
- Redis caching uses Jackson polymorphic typing so cached lists and DTOs
  deserialize back into their concrete classes.
