# Environment Variables

Copy `.env.example` to `.env` in the repo root and fill in real values. `docker-compose.yml` loads this file into the `backend` service via `env_file`; the same variables can be exported directly when running `./gradlew bootRun` or `npm run dev` outside Docker.

| Variable | Used by | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | backend | JDBC URL of the PostgreSQL database (Supabase pooler URL in production, e.g. `jdbc:postgresql://<host>:5432/postgres?sslmode=require`) |
| `SPRING_DATASOURCE_USERNAME` | backend | Database username |
| `SPRING_DATASOURCE_PASSWORD` | backend | Database password |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | backend | Must stay `none` — schema is owned by Flyway, not Hibernate (see [database.md](database.md)) |
| `SPRING_JPA_SHOW_SQL` | backend | Logs generated SQL when `true`; keep `false` outside local debugging |
| `SPRING_FLYWAY_ENABLED` | backend | Runs pending migrations on startup when `true` |
| `JWT_SECRET` | backend | HMAC signing key for JWTs, random base64, minimum 32 bytes |
| `MAPBOX_TOKEN` | backend | Mapbox token used server-side (e.g. reverse geocoding, if applicable) |
| `VITE_MAPBOX_TOKEN` | frontend (build-time) | Mapbox public token baked into the frontend bundle for the map UI |
| `VITE_API_URL` | frontend (build-time) | Base URL the frontend uses to call the backend (e.g. `http://localhost:8080`, or `http://backend:8080` inside the Docker network) |
| `CORS_ALLOWED_ORIGINS` | backend | Comma-separated list of origins allowed to call the API and connect to the WebSocket (`CorsConfig`, `WebSocketConfig`) |
| `SONAR_TOKEN` | CI | Authentication token for pushing analysis to SonarQube |

## Hardware secrets

The ESP32 firmware does **not** read from `.env`. Wi-Fi credentials and the device's API key (obtained from `POST /api/devices/register`) go in `hardware/src/secrets.h`, which is gitignored. Copy `hardware/src/secrets.h.example` and fill in real values before flashing the device.

## CI/CD-only variables

Set as GitLab CI/CD variables (not in `.env`):

| Variable | Purpose |
|---|---|
| `GCP_SA_KEY` | GCP service account key JSON, used by `deploy-backend`/`deploy-frontend` to authenticate `gcloud` |
| Docker Hub credentials | Used by `docker-backend`/`docker-frontend` to push images to `rubberduck808/tripwire-backend` / `tripwire-frontend` |
