# CI/CD & Deployment

## Pipeline overview (`.gitlab-ci.yml`)

Five stages, run in order: **build → test → sonar → docker → deploy**.

| Stage | Jobs | Notes |
|---|---|---|
| build | `backend-build`, `frontend-build` | Compiles only, no tests. Path-filtered: a job only runs if files under its own directory changed. |
| test | `backend-test`, `frontend-test`, `frontend-e2e` | `backend-test` runs against `docker-compose.test.yml` (ephemeral Postgres). `frontend-e2e` runs Playwright against `docker-compose.e2e.yml`/`docker-compose.e2e.local.yml` and is allowed to fail without blocking the pipeline (`allow_failure: true`). |
| sonar | `sonarqube-backend`, `sonarqube-frontend` | Pushes JaCoCo (backend) and coverage-v8 (frontend) reports to a SonarQube instance (`SONAR_HOST_URL`, default `http://host.docker.internal:9000`). |
| docker | `docker-backend`, `docker-frontend` | Builds and pushes images to Docker Hub (`rubberduck808/tripwire-backend`, `rubberduck808/tripwire-frontend`) using Docker-in-Docker. |
| deploy | `deploy-backend`, `deploy-frontend` | Deploys the pushed images to **Google Cloud Run**, region `europe-west4`, project `tripwire-477820`. Only runs on `main`, and only when relevant files changed. |

Path-based `rules: changes:` filters mean a docs-only or hardware-only change skips backend/frontend build, test, and deploy jobs entirely.

### Deploy targets

- `tripwire-backend` Cloud Run service — image `docker.io/rubberduck808/tripwire-backend:latest`, port 8080, unauthenticated.
- `tripwire-frontend` Cloud Run service — image `docker.io/rubberduck808/tripwire-frontend:latest`, port 80, unauthenticated.

Deploy auth uses a GCP service account key stored in the `GCP_SA_KEY` CI variable.

## Docker Compose files

| File | Purpose |
|---|---|
| `docker-compose.yml` | Local dev: builds `backend` (port 8080) and `frontend` (port 5173→80), backend reads secrets from `.env`. Assumes an external Postgres (Supabase, hosted or local via `supabase start`). |
| `docker-compose.test.yml` | Ephemeral Postgres (`test`/`test`/`testdb`) for backend unit/integration tests with TestContainers-style isolation. |
| `docker-compose.e2e.yml` | Full stack (Postgres + backend + frontend) for CI E2E runs. Does **not** publish ports — CI containers talk over the compose network only. |
| `docker-compose.e2e.local.yml` | Same as above but with ports published, for running Playwright E2E tests locally against a containerized stack. |
| `docker-compose.sonar.yml` | Local SonarQube + its own Postgres, for running static analysis against the same dashboard CI uses. |

## Environment & secrets

- App secrets (DB credentials, JWT secret, Mapbox token, CORS origins) are supplied via `.env` locally and CI/CD variables in the pipeline — see [environment.md](environment.md).
- Hardware secrets (Wi-Fi credentials, device API key) live in `hardware/src/secrets.h`, gitignored, copied from `secrets.h.example`.
- GCP deploy credentials are stored as the `GCP_SA_KEY` GitLab CI/CD variable, never committed.
