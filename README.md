# ticketing-platform

대용량 트래픽(선착순 오픈)을 견디는 티켓 예매 플랫폼. 학습/포트폴리오 목적의 모노레포.

## 구성

| 모듈 | 스택 | 설명 |
|------|------|------|
| `ticketing-api` | Java 21 · Spring Boot 3.5 · JPA + QueryDSL · Flyway · Redis | 백엔드 REST API |
| `ticketing-web` | Node 22 · React 19 · Vite 7 · TypeScript · TanStack Query | 프론트엔드 SPA |
| `docker-compose.yml` | MySQL 8 (호스트 13306) · Redis 7 (호스트 16379) | 로컬 인프라 — 네이티브 3306/6379와 충돌 방지 |

## 빠른 시작

```bash
# 1. 인프라 기동
docker compose up -d

# 2. 백엔드 (http://localhost:8080, Swagger: /swagger-ui.html)
cd ticketing-api
./gradlew bootRun

# 3. 프론트엔드 (http://localhost:5173, /api → 8080 프록시)
cd ticketing-web
npm install
npm run dev
```

> `ticketing-api`는 Gradle toolchain으로 JDK 21을 자동 관리한다. 로컬에 JDK 21이 없으면
> Gradle이 받아온다(foojay-resolver).

## 테스트

```bash
cd ticketing-api
./gradlew test              # 단위 / 웹 슬라이스 테스트 — Docker 불필요
./gradlew integrationTest   # Testcontainers 통합 테스트 — Docker 데몬 필요
```

`@Tag("integration")` 이 붙은 테스트는 `test` 태스크에서 제외되고 `integrationTest` 로 분리된다.
CI(GitHub Actions)는 두 태스크를 모두 실행한다.

> 로컬에서 `integrationTest` 는 Docker Engine 29 + Testcontainers 1.21.3 호환 문제로 실패할 수
> 있다(코드 문제 아님, CI 는 정상). 로컬 검증은 `docker compose up -d` 후 `./gradlew bootRun` 으로
> 실제 스택에 대해 수행한다.

## 현재 상태

**공통 인프라**
- 공통 응답 포맷(`ApiResponse`), 전역 예외 처리 — 비즈니스 예외 / 검증 실패(400) /
  라우트 없음(404) / 메서드 불가(405) / 인증(401) / 권한(403) 매핑
- 요청별 correlation-id 필터(`X-Request-Id`) + 접근 로그, JPA Auditing, Redis 템플릿, OpenAPI, CORS
- 가상 스레드 활성화, HikariCP 튜닝, graceful shutdown
- 로컬 전용 시드(`db/seed/R__dev_seed.sql`, `local` 프로필에서만 로드)
- CI: `.github/workflows/ci.yml` — api 빌드/테스트/통합테스트 + web lint/build

**도메인**

| 도메인 | 상태 |
|--------|------|
| `member` / `auth` | ✅ 회원가입 · 로그인 · 토큰 재발급 · `GET /members/me`. BCrypt + JWT(access/refresh) + `SecurityConfig` + `@CurrentMember` |
| `performance` | ✅ 목록 / 단건 조회 (읽기 전용) |
| `seat` | ✅ Schedule/SeatGrade/Seat 스키마 + 좌석맵 조회 API |
| `reservation` | ✅ 좌석 선점(3가지 락 전략) → 예매(PENDING) → confirm/cancel → 만료 스케줄러 |
| `waitingqueue` | ✅ Redis ZSet 대기열 + 승격 스케줄러 + 입장 토큰 인터셉터 |
| `payment` | ✅ 모의 PG + Idempotency-Key 멱등 결제 + 예매 확정 연동 |

### 결제 API (인증 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/payments` | `Idempotency-Key` 헤더 필수. `{reservationId}` 결제 → 성공 시 예매 CONFIRMED + 좌석 SOLD |
| GET | `/api/v1/payments/{id}` | 결제 단건 |

- 동일 `Idempotency-Key` 재요청 → 최초 결과를 재생(재청구 없음). 다른 요청에 재사용 → 409.
- PG 호출은 짧은 트랜잭션 2개(준비/반영) 사이에서 수행. `app.payment.pg.failure-rate` 로 실패 시뮬레이션.
- 미결제 자동 취소는 `reservation` 만료 스케줄러가 담당(PENDING + hold 만료 → 좌석 반환).

### 대기열 API (인증 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/waiting-queue/{scheduleId}/enter` | 대기열 진입 → 순번 |
| GET | `/api/v1/waiting-queue/{scheduleId}/status` | 순번 / 입장 여부 / `entryToken` |

`app.waiting-queue.enforce=true` 이면 `POST /reservations` 는 승격으로 발급된
`X-Queue-Token` 헤더를 요구한다(없으면 429). 스케줄러가 `promote-interval` 마다
ZSet 앞에서 `promote-batch-size` 명을 뽑아 TTL 기반 active 상태 + 입장 토큰을 발급.

### 예매 API (인증 필요)

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/reservations` | 좌석 선점 → PENDING 예매 생성. `?lockStrategy=PESSIMISTIC\|OPTIMISTIC\|DISTRIBUTED` 로 전략 override (벤치마크용) |
| GET | `/api/v1/reservations` | 내 예매 목록 |
| GET | `/api/v1/reservations/{id}` | 예매 단건 (본인 것만) |
| POST | `/api/v1/reservations/{id}/confirm` | 결제 확정 (좌석 SOLD) — 결제 도메인이 추후 래핑 |
| POST | `/api/v1/reservations/{id}/cancel` | 예매 취소 (좌석 반환) |

**락 전략 비교** (`domain/reservation/lock/`)

| 전략 | 메커니즘 | 특징 |
|------|----------|------|
| `PESSIMISTIC` | `SELECT ... FOR UPDATE` | 경합 직렬화, 대기 발생, 확실 |
| `OPTIMISTIC` | `@Version` + 재시도(최대 3회) | 락 없음, 경합 시 재시도 비용 |
| `DISTRIBUTED` | Redis `SET NX` + Lua unlock | DB 락 없이 분산 환경 대응, Redis 의존 |

24-thread 동시 요청 시 세 전략 모두 정확히 1건만 성공 (`ReservationConcurrencyIT`).

### 인증 API

| 메서드 | 경로 | 설명 |
|--------|------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 → `{accessToken, refreshToken, tokenType, expiresIn}` |
| POST | `/api/v1/auth/refresh` | refresh 토큰으로 재발급 |
| GET | `/api/v1/members/me` | 현재 회원 (Bearer 필요) |

공개 엔드포인트: `POST /auth/**`, `GET /performances/**`, `/actuator/health`, Swagger. 그 외는 인증 필요.

## 로드맵

1. ~~`member` 인증 + JWT 필터 + SecurityConfig~~ ✅
2. ~~`seat` 스키마(V3) + 좌석맵 조회 API~~ ✅
3. ~~`reservation` 좌석 선점/예매 — 비관적/낙관적/분산 락 3전략~~ ✅
4. ~~`waitingqueue` Redis ZSet 대기열 + 입장 토큰 인터셉터~~ ✅
5. ~~`payment` PG 모의 + Idempotency-Key + 미결제 자동 취소~~ ✅
6. k6 부하 테스트 + 메트릭(Prometheus/Grafana)
