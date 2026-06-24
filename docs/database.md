# Database

PostgreSQL, hosted on Supabase in production. Schema is owned entirely by **Flyway** migrations in `backend/src/main/resources/db/migration/` — Hibernate is configured to validate against this schema, not generate it, so any schema change must go through a new migration file.

## Migration history

| Migration | Purpose |
|---|---|
| `V1__Initial_schema.sql` | Core schema: `role`, `app_user`, `friendship`, `favourite_location`, `achievement`, `user_achievement`, `hazard_category`, `hazard_report`, `vote`, `license_plate_info`, `statistics`, `trip` |
| `V2__Add_missing_tables.sql` | Adds `avatar`, `background`, `device`, `device_ownership`, `live_telemetry`, `telemetry_history`, `telemetry`; links `app_user` to `avatar`/`background`; adds trip start/end coordinates, `created_at`, `convoy_id`; adds `icon_path`/`active` to `hazard_category` |
| `V3__Add_unique_vote_user_hazard.sql` | Adds a uniqueness constraint so a user can only have one vote per hazard report |
| `V4__Seed_roles.sql` | Seeds `role` with `USER` and `ADMIN` |
| `V5__Seed_hazard_categories.sql` | Seeds `hazard_category` with `Pothole`, `Accident`, `Debris`, `Construction`, `Flood` |
| `V6__Add_user_active_field.sql` | Adds an `active` flag to `app_user` for soft deactivation by admins |

## Core tables and relationships

- **`app_user`** — account record (username, password hash, role FK, `avatar_id`, `background_id`, `active`). One user can own many `device`s (via `device_ownership`), have many `friendship`s, `trip`s, `hazard_report`s, `vote`s, and `user_achievement`s.
- **`role`** — `USER` / `ADMIN`, referenced by `app_user`.
- **`friendship`** — a pair of users plus a status (`PENDING` / `ACCEPTED` / `BLOCKED`, see `FriendshipStatus`).
- **`hazard_category`** — lookup table for hazard types (icon path, active flag).
- **`hazard_report`** — a reported hazard: location, category FK, reporting user FK, status (`HazardStatus`).
- **`vote`** — a user's upvote/downvote (`VoteType`) on a `hazard_report`; unique per `(user, hazard)` since `V3`.
- **`device`** — a registered ESP32 unit (hardware device id, API key).
- **`device_ownership`** — links a `device` to the `app_user` who currently owns/drives it (supports transfer/unassign).
- **`live_telemetry`** — the latest telemetry snapshot per device (overwritten on each update).
- **`telemetry_history` / `telemetry`** — persisted time-series telemetry readings for a device.
- **`trip`** — a recorded journey: start/end lat-lng, timestamps, `convoy_id` (for group trips), owning user FK.
- **`statistics`** — aggregated per-user driving stats.
- **`achievement` / `user_achievement`** — gamification badges and which users have earned them.
- **`favourite_location`** — saved locations per user.
- **`license_plate_info`** — vehicle metadata associated with a device/user.
- **`avatar` / `background`** — profile customization image catalogs, referenced by `app_user`.

## Adding a migration

Create a new `V{n}__Description.sql` file with the next sequential version number — Flyway applies pending migrations in order on application startup and will refuse to start if an already-applied migration's checksum changes. Never edit an already-applied migration file; add a new one instead.
