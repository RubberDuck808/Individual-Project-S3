# Testing

## Backend

`backend/src/test/java/nl/fontys/db3/backend/` — 38 test classes split into:

- **`service/`** — unit tests for business logic, mocked repositories (JUnit 5 + Mockito).
- **`integration/controller/`** — full Spring context tests hitting controllers through the HTTP layer.
- **`integration/repository/`** — JPA repository tests against a real database.

Run:

```sh
cd backend
./gradlew test
```

Integration tests run against `docker-compose.test.yml`'s ephemeral Postgres container (`test`/`test`/`testdb`) so they exercise real Flyway migrations and SQL, not an in-memory substitute. Coverage is collected with JaCoCo and uploaded to SonarQube in CI (`sonarqube-backend` job).

## Frontend

Unit testing is wired up via Vitest (`npm test`, `npm run test:coverage`, config in `frontend/vitest.config.js`), but at present there are no unit test files under `frontend/src` — test coverage for the frontend currently comes from the Playwright E2E suite instead.

### E2E (Playwright)

`frontend/e2e/`:

| Spec | Covers |
|---|---|
| `login.spec.e2e.js` | Login flow |
| `friendship-flow.spec.e2e.js` | Sending/accepting/declining friend requests |
| `report-flow.spec.e2e.js` | Creating and viewing a hazard report |

Run against a real backend + frontend + Postgres stack:

```sh
cd frontend
npm install
npm run test:e2e          # headless
npm run test:e2e:headed   # visible browser
npm run test:e2e:ui       # Playwright UI mode
```

In CI, the `frontend-e2e` job brings the stack up via `docker-compose.e2e.yml` and is marked `allow_failure: true`, so a flaky E2E run won't block the pipeline. For a local equivalent of that containerized run (with ports published so Playwright on your host can reach it), use `docker-compose.e2e.local.yml`.

## SonarQube (local)

To run the same static analysis the CI `sonar` stage runs, bring up a local instance:

```sh
docker-compose -f docker-compose.sonar.yml up
# SonarQube UI at http://localhost:9000
cd frontend && npm run sonar
cd backend && ./gradlew sonar
```
