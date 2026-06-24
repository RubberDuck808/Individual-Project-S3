# API Reference

Base URL (local dev): `http://localhost:8080`

All endpoints below are prefixed as shown. Unless noted, endpoints require a `Authorization: Bearer <jwt>` header (obtained from `POST /api/users/login`). Admin-only endpoints additionally require the `ADMIN` role.

## Auth & Users — `/api/users`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Create a new user account |
| POST | `/login` | Authenticate, returns a JWT |
| GET | `/me` | Get the current authenticated user's profile |
| PUT | `/me` | Update the current user's profile |
| GET | `/{username}` | Get a public profile by username |
| PUT | `/me/avatar` | Set the current user's avatar |
| PUT | `/me/background` | Set the current user's profile background |
| GET | `/{username}/stats` | Get a user's driving statistics (`StatisticsController`) |

## Hazard reports — `/api/hazards`

| Method | Path | Description |
|---|---|---|
| GET | `/open` | List currently open hazard reports |
| POST | `/` | Create a new hazard report |
| GET | `/by-user/{username}` | List hazards reported by a given user |

Hazard create/update/delete events are also broadcast over WebSocket on `/topic/hazards` (see [architecture.md](architecture.md#real-time-messaging)).

## Hazard categories — `/api/hazard-categories`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List available hazard categories (seeded via migration `V5`) |

## Votes — `/api/votes`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List votes |
| POST | `/` | Cast or change a vote (upvote/downvote) on a hazard |
| GET | `/{hazardId}/count` | Get the vote tally for a hazard |
| GET | `/user/{username}/cast` | List votes cast by a user |
| GET | `/{hazardId}/mine` | Get the current user's vote on a hazard |

## Trips — `/api/trips`

| Method | Path | Description |
|---|---|---|
| POST | `/complete` | Submit a completed trip (route, timestamps, distance) |

## Telemetry — `/api/telemetry`

| Method | Path | Description |
|---|---|---|
| PUT | `/live` | Push a live telemetry reading (called by the ESP32 device, API-key auth) |
| GET | `/live/{deviceId}` | Get the latest live reading for a device |
| POST | `/history` | Persist a telemetry reading to history |
| GET | `/history/{deviceId}` | Get telemetry history for a device |
| GET | `/history/{deviceId}/range` | Get telemetry history for a device within a time range |
| GET | `/device/{deviceId}/health` | Get current derived health status for a device |
| GET | `/device/{deviceId}/health/history` | Get historical health status for a device |

Live telemetry is also pushed over WebSocket on `/topic/telemetry/{deviceId}` and the aggregate `/topic/telemetry`.

## Devices — `/api/devices`

| Method | Path | Description |
|---|---|---|
| POST | `/register` | Register a new ESP32 device and obtain its API key |
| POST | `/{deviceId}/assign` | Assign a device to the current user |
| POST | `/{deviceId}/transfer` | Transfer device ownership to another user |
| DELETE | `/{deviceId}/unassign` | Remove the current ownership link |
| GET | `/my-devices` | List devices owned by the current user |
| GET | `/{deviceId}/ownership` | Get ownership info for a device |

## Friendships — `/api/friendships`

| Method | Path | Description |
|---|---|---|
| POST | `/request` | Send a friend request |
| POST | `/accept/{username}` | Accept an incoming request |
| DELETE | `/decline/{username}` | Decline an incoming request |
| DELETE | `/cancel/{username}` | Cancel an outgoing request |
| DELETE | `/unfriend/{username}` | Remove an existing friendship |
| GET | `/requests/incoming` | List incoming pending requests |
| GET | `/requests/outgoing` | List outgoing pending requests |
| GET | `/` | List current friendships |
| GET | `/user/{username}` | Get friendship status with a specific user |

## Profile assets

| Method | Path | Description |
|---|---|---|
| GET | `/api/avatars` | List available avatars |
| GET | `/api/backgrounds` | List available backgrounds |

## Admin — `/api/admin`

| Method | Path | Description |
|---|---|---|
| GET | `/statistics` | Platform-wide statistics |
| GET | `/users` | List all users |
| GET | `/users/{id}` | Get a user by id |
| PUT | `/users/{id}/role` | Change a user's role |
| DELETE | `/users/{id}` | Delete/deactivate a user |

### Admin devices — `/api/admin/devices`

| Method | Path | Description |
|---|---|---|
| GET | `/` | List all devices |
| GET | `/{id}` | Get a device by id |
| GET | `/device-id/{deviceId}` | Get a device by its hardware device id |
| PUT | `/{id}/activate` | Activate a device |
| PUT | `/{id}/deactivate` | Deactivate a device |
| PUT | `/{id}/description` | Update a device's description |

### Admin assets — `/api/admin/assets`

| Method | Path | Description |
|---|---|---|
| GET | `/avatars` | List avatars (admin view) |
| GET | `/avatars/{id}` | Get an avatar |
| POST | `/avatars` | Upload a new avatar |
| PUT | `/avatars/{id}` | Update an avatar |
| DELETE | `/avatars/{id}` | Delete an avatar |
| PUT | `/avatars/{id}/deactivate` | Deactivate an avatar |
| GET | `/backgrounds` | List backgrounds (admin view) |
| GET | `/backgrounds/{id}` | Get a background |
| POST | `/backgrounds` | Upload a new background |
| PUT | `/backgrounds/{id}` | Update a background |
| DELETE | `/backgrounds/{id}` | Delete a background |
| PUT | `/backgrounds/{id}/deactivate` | Deactivate a background |

## WebSocket (STOMP over SockJS)

Endpoint: `ws://localhost:8080/ws` (SockJS fallback enabled, JWT validated on `CONNECT`)

| Topic | Published when |
|---|---|
| `/topic/hazards` | A hazard report is created, updated, or deleted |
| `/topic/telemetry/{deviceId}` | A specific device pushes a new live telemetry reading |
| `/topic/telemetry` | Any device pushes a new live telemetry reading (aggregate feed) |
