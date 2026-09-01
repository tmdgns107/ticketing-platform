# ticketing-platform

대용량 트래픽(선착순 오픈)을 견디는 티켓 예매 플랫폼. 학습/포트폴리오 목적의 모노레포.

## 구성

| 모듈 | 스택 | 설명 |
|------|------|------|
| `ticketing-api` | Java 21 · Spring Boot 3.5 · JPA + QueryDSL · Flyway · Redis | 백엔드 REST API |
| `ticketing-web` | Node 22 · React 19 · Vite 7 · TypeScript · TanStack Query | 프론트엔드 SPA |
| `docker-compose.yml` | MySQL 8 · Redis 7 | 로컬 인프라 |

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

## 현재 상태 (틀만 잡힌 단계)

구현됨:
- 공통 응답 포맷(`ApiResponse`), 전역 예외 처리 — 비즈니스 예외 / 검증 실패 / 타입 미스매치(400) /
  라우트 없음(404) / 메서드 불가(405) 매핑
- 요청별 correlation-id 필터(`X-Request-Id`) + 접근 로그, JPA Auditing, Redis 템플릿, OpenAPI, CORS
- 가상 스레드 활성화(`spring.threads.virtual.enabled`), HikariCP 튜닝, graceful shutdown
- `performance` 도메인 수직 슬라이스: 엔티티 · 리포지토리 · 서비스 · 컨트롤러 · Flyway `V1__init.sql`
- 로컬 전용 시드(`db/seed/R__dev_seed.sql`, `local` 프로필에서만 로드)
- 프론트: 라우팅, QueryClient, axios 클라이언트(토큰 주입 + 401 처리), 공연 목록/상세 페이지
- CI: `.github/workflows/ci.yml` — api 빌드/테스트 + web lint/build

패키지만 잡아둔 도메인 (`package-info.java`에 책임 명시):
- `member` — 가입/인증(JWT)
- `waitingqueue` — Redis Sorted Set 기반 입장 대기열 + 승격 스케줄러
- `seat` — 회차/좌석/등급, Redis 좌석 선점
- `reservation` — 예매 상태 머신, 동시성 제어 비교, 만료 스케줄러
- `payment` — PG 모의 연동, 멱등키 중복 결제 방지

## 로드맵

1. `member` 인증 + JWT 필터 + SecurityConfig
2. `seat` 스키마(V2) + 좌석 조회 API
3. `reservation` 좌석 선점/예매 — 비관적 락 vs 낙관적 락 vs Redis 분산 락 벤치마크
4. `waitingqueue` 대기열 + 입장 토큰 인터셉터
5. `payment` 멱등 결제 + 미결제 자동 취소 스케줄러
6. k6 부하 테스트 시나리오, 리드 레플리카 / 캐시 계층 도입
