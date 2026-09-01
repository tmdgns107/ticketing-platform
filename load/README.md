# Load tests (k6) & observability

## Prerequisites

- API running against the docker-compose infra (`docker compose up -d && ./gradlew bootRun` in `ticketing-api`)
- [k6](https://k6.io/docs/get-started/installation/) (`brew install k6`)

## Scenarios

| Script | What it does |
|--------|--------------|
| `k6/lock-benchmark.js` | 100 VUs fight over a small seat pool with one lock strategy. Compare `hold_won` / `hold_conflict` / `hold_latency_ms` p95 across `-e STRATEGY=PESSIMISTIC\|OPTIMISTIC\|DISTRIBUTED`. |
| `k6/seat-rush.js` | Full funnel per VU: enter queue → poll → reserve → pay. Run the API with `QUEUE_ENFORCE=true`. |

```bash
# lock strategy comparison (reset seats between runs)
k6 run -e STRATEGY=PESSIMISTIC load/k6/lock-benchmark.js
k6 run -e STRATEGY=OPTIMISTIC  load/k6/lock-benchmark.js
k6 run -e STRATEGY=DISTRIBUTED load/k6/lock-benchmark.js

# on-sale rush
k6 run -e VUS=500 -e DURATION=2m load/k6/seat-rush.js
```

Reset seat state between runs by restarting the API against a fresh DB volume
(`docker compose down -v && docker compose up -d`) — the dev seed rebuilds the
80-seat grid.

## Metrics stack

```bash
docker compose --profile monitoring up -d
```

- API exposes Prometheus at `http://localhost:8080/actuator/prometheus`
- Prometheus UI: `http://localhost:9090`
- Grafana: `http://localhost:3000` (admin / admin) — "Ticketing" dashboard is auto-provisioned

Key app metric: `ticketing_reservation_hold_seconds{strategy,outcome}` — timer +
count of seat-hold attempts, tagged by lock strategy and outcome
(`won` / `taken` / `lock_conflict` / `error`).
