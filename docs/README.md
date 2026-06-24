# TripWire Documentation

| Doc | Covers |
|---|---|
| [architecture.md](architecture.md) | How the backend, frontend, and ESP32 firmware fit together; layering, auth, real-time messaging |
| [api.md](api.md) | Full REST endpoint reference and WebSocket/STOMP topics |
| [database.md](database.md) | Schema, Flyway migration history, table relationships |
| [hardware.md](hardware.md) | ESP32 firmware structure, data-source modes, build/flash workflow |
| [testing.md](testing.md) | Backend (JUnit/Mockito), frontend (Vitest), and E2E (Playwright) test setup |
| [deployment.md](deployment.md) | GitLab CI/CD pipeline stages and Cloud Run deployment |
| [environment.md](environment.md) | Every environment variable, what reads it, and where to set it |

For a deep, file-by-file backend walkthrough, see [`../BACKEND_GUIDE.md`](../BACKEND_GUIDE.md) at the repo root.
