# TripWire

A driving companion application: drivers report and view road hazards on a live map, track GPS trips, see vehicle telemetry streamed from an ESP32 device, and connect with friends. Admins manage users, devices, and profile assets from a dedicated dashboard.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Java 21, Spring Boot, Spring Security (JWT), Spring WebSocket (STOMP), JPA, Flyway, PostgreSQL |
| Frontend | React 19, Vite, Tailwind CSS, Mapbox GL |
| Hardware | ESP32 (PlatformIO, C++) |
| Cloud | GCP Cloud Run, Docker Hub, Supabase |
| CI/CD | GitLab CI |

## Project Structure

```
individual-project-s3/
  backend/       Spring Boot REST + WebSocket API
  frontend/      React SPA (Vite)
  hardware/      ESP32 firmware (PlatformIO)
  supabase/      Database config
  docs/          Full documentation (see below)
```

## Documentation

Detailed docs live in [`docs/`](docs/README.md):

- [Architecture](docs/architecture.md) — backend layering, auth, real-time messaging, frontend routing
- [API reference](docs/api.md) — every REST endpoint and WebSocket topic
- [Database](docs/database.md) — schema and Flyway migration history
- [Hardware](docs/hardware.md) — ESP32 firmware structure and build/flash workflow
- [Testing](docs/testing.md) — backend, frontend, and E2E test setup
- [Deployment](docs/deployment.md) — CI/CD pipeline and Cloud Run deploy
- [Environment variables](docs/environment.md) — every variable, what reads it, where to set it

A deep, file-by-file backend walkthrough also exists at [`BACKEND_GUIDE.md`](BACKEND_GUIDE.md).

## Local Development

### Prerequisites
- Java 21, Gradle
- Node.js 20+
- Docker (for local PostgreSQL or `docker-compose`)

### 1. Configure environment

Copy `.env.example` to `.env` and fill in your credentials (see [docs/environment.md](docs/environment.md) for what each variable does):

```sh
cp .env.example .env
```

### 2. Run with Docker Compose

```sh
docker-compose up
```

This starts the backend (port 8080) and frontend (port 5173).

### 3. Run backend only

```sh
cd backend
./gradlew bootRun
```

### 4. Run frontend only

```sh
cd frontend
npm install
npm run dev
```

## Running Tests

```sh
cd backend && ./gradlew test          # backend unit + integration tests
cd frontend && npm test               # frontend unit tests (Vitest)
cd frontend && npm run test:e2e       # E2E tests (Playwright)
```

See [docs/testing.md](docs/testing.md) for what each suite actually covers and how it runs in CI.

## Hardware (ESP32)

Firmware lives in `hardware/` (PlatformIO project, board `esp32dev`). Device secrets (Wi-Fi credentials, API key) go in `hardware/src/secrets.h`, gitignored — copy from `secrets.h.example`. See [docs/hardware.md](docs/hardware.md) for firmware structure and the build/flash workflow.

## CI/CD & Deployment

GitLab CI runs build → test → sonar → docker → deploy, deploying to Google Cloud Run on merges to `main`. See [docs/deployment.md](docs/deployment.md).
