# Architecture

TripWire is a three-part system: a Spring Boot REST/WebSocket API, a React single-page frontend, and ESP32 firmware that streams vehicle telemetry into the backend.

```
┌─────────────┐        HTTPS / WSS        ┌──────────────────┐        SQL        ┌────────────────┐
│   Frontend  │ ─────────────────────────▶ │      Backend     │ ─────────────────▶│   PostgreSQL    │
│ React+Vite  │ ◀───────────────────────── │  Spring Boot API │ ◀──────────────── │   (Supabase)    │
└─────────────┘                            └──────────────────┘                    └────────────────┘
                                                     ▲
                                                     │ HTTPS (API key auth)
                                                     │
                                            ┌──────────────────┐
                                            │   ESP32 Device    │
                                            │  (PlatformIO/C++) │
                                            └──────────────────┘
```

## Backend

Package root: `backend/src/main/java/nl/fontys/db3/backend`

Layered, N-tier structure:

```
controller/   → REST endpoints, request/response only, no business logic
service/      → business logic, transaction boundaries
repository/   → Spring Data JPA repositories
entity/       → JPA-mapped domain model
dto/          → request/response payloads, never expose entities directly
mapper/       → MapStruct entity ↔ DTO conversion
exception/    → @ControllerAdvice global error handling
security/     → JWT auth, device API-key auth, WebSocket auth
config/       → Spring configuration beans (CORS, security, WebSocket, storage)
```

Requests flow as: `Filter (JWT / API key) → Controller → Service → Repository → Entity`.

### Authentication

Two parallel auth mechanisms, both implemented as `OncePerRequestFilter`s registered in `SecurityConfig`:

- **`JwtAuthFilter`** — validates a bearer JWT (issued at `/api/users/login`) for normal user/admin requests. `JwtService` handles signing/parsing; `CustomUserDetailsService` loads the user.
- **`DeviceApiKeyAuthFilter`** — validates a per-device API key (issued at `/api/devices/register`) for telemetry-ingestion requests coming from ESP32 hardware, since a device can't hold a user session.

`AuthRateLimitInterceptor` throttles repeated calls to login/register endpoints to slow down brute-force attempts.

Roles are `USER` and `ADMIN` (see `Role` entity); admin-only endpoints are guarded by Spring Security method/route rules in `SecurityConfig`.

### Real-time messaging

Configured in `WebSocketConfig`: a STOMP endpoint at `/ws` (SockJS fallback enabled), app-destination prefix `/app`, simple in-memory broker on `/topic`.

Two publisher services push server-initiated events to subscribed clients:

| Publisher | Topic | Payload |
|---|---|---|
| `HazardWsPublisher` | `/topic/hazards` | hazard created/updated (`upsert`) or removed (`delete`) |
| `TelemetryWsPublisher` | `/topic/telemetry/{deviceId}` and `/topic/telemetry` | live telemetry reading for a device, and a global feed |

`WebSocketAuthConfig` attaches JWT validation to the STOMP `CONNECT` frame so only authenticated users can subscribe.

### Database

PostgreSQL, hosted on Supabase, schema owned by Flyway migrations under `backend/src/main/resources/db/migration/` (see [database.md](database.md)). Hibernate/JPA maps the `entity/` classes onto that schema — `ddl-auto` is **not** used to generate schema; Flyway is the single source of truth.

### Storage

Avatars and backgrounds are binary assets, stored via a storage abstraction configured in `StorageProperties`. `AvatarController`/`BackgroundController` serve catalog metadata; `AdminAssetController` lets admins upload, update, deactivate, or delete assets.

## Frontend

Root: `frontend/src`

```
pages/        → route-level screens (one per page in App.jsx)
pages/admin/  → admin-only screens, lazy-loaded and code-split
components/   → reusable UI (map markers, car widgets, profile cards, hazard forms...)
layouts/      → MainLayout (user shell) and AdminLayout (admin shell)
api/          → one module per backend resource, wraps fetch + auth headers
hooks/        → reusable stateful logic (geolocation, websocket subscriptions, etc.)
context/      → AssetsCacheContext — caches avatar/background catalogs client-side
providers/    → RealLocationProvider — wraps the browser Geolocation API
utils/        → formatting/helpers shared across components
```

Routing (`App.jsx`) has three tiers:

1. **Public** — `/`, `/login`, `/signup`
2. **Protected (user)** — wrapped in `ProtectedRoute`, rendered inside `MainLayout`: `/map`, `/profile/:username`, `/car`, `/settings`
3. **Protected (admin)** — wrapped in `ProtectedRoute requireRole="ADMIN"`, rendered inside a lazily-loaded `AdminLayout`: `/admin`, `/admin/users`, `/admin/devices`, `/admin/assets`

`ProtectedRoute` checks the stored JWT (and role, for admin routes) before rendering its children, redirecting to `/login` otherwise.

Live map and telemetry views subscribe to the backend's STOMP topics directly from the browser via `@stomp/stompjs`, so hazard pins and vehicle stats update without polling.

## Hardware (ESP32)

`hardware/src/main.cpp`, built with PlatformIO (`hardware/platformio.ini`, board `esp32dev`, 115200 baud). The firmware:

1. Connects to Wi-Fi.
2. Authenticates to the backend with a per-device API key issued by `POST /api/devices/register`.
3. Periodically POSTs telemetry readings (e.g. speed, location, diagnostics) to the backend's telemetry endpoint.

Device secrets (Wi-Fi credentials, API key) live in `hardware/src/secrets.h`, which is gitignored — copy it from `secrets.h.example` and fill in real values locally; never commit it.

## CI/CD

`.gitlab-ci.yml` defines a multi-stage pipeline: build → test → SonarQube scan → Docker image build → deploy. Backend coverage is collected via JaCoCo, frontend via Vitest's `coverage-v8`, both surfaced in SonarQube. See [deployment.md](deployment.md) for the deploy targets.
