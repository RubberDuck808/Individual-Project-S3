# Hardware (ESP32 Firmware)

`hardware/`, built with PlatformIO, board `esp32dev`, Arduino framework, 115200 baud.

## Source layout (`hardware/src/`)

```
main.cpp        → setup()/loop(), wires the components below together
config/         → Config.h: data-source mode, send intervals, backend URL defaults
identity/       → DeviceIdentity: generates/persists a stable device ID on first boot
data/           → DataSourceFactory + IDataSource implementations (simulated vs. real OBD-II)
sender/         → TelemetrySender: HTTP client that posts readings to the backend, API-key auth
manager/        → TelemetryManager: schedules live vs. history sends at their respective intervals
network/        → Wi-Fi connection handling (initWiFi)
model/          → telemetry data structures shared across the above
utils/          → SerialCommands: serial-console debug commands
```

## Data source modes (`config/Config.h`)

```cpp
#define DATA_SOURCE_MODE DATA_SOURCE_SIMULATED   // or DATA_SOURCE_REAL
```

- `DATA_SOURCE_SIMULATED` — generates plausible fake telemetry, useful for developing/testing the backend and frontend without physical hardware.
- `DATA_SOURCE_REAL` — reads from real OBD-II hardware attached to the ESP32.

`DataSourceFactory::create()` returns the right `IDataSource` implementation based on this flag, so `main.cpp` and `TelemetryManager` don't need to know which mode is active.

## Send intervals

- `LIVE_TELEMETRY_INTERVAL_MS` (default 1000ms) — pushed to `PUT /api/telemetry/live`, also fanned out over `/topic/telemetry/{deviceId}`.
- `HISTORY_TELEMETRY_INTERVAL_MS` (default 60000ms) — pushed to `POST /api/telemetry/history` for long-term storage.

## Device identity

`DeviceIdentity::initialize()` generates a stable device ID on first boot and persists it (so re-flashing doesn't change the device's identity), unless a `DEVICE_ID` override is set in `secrets.h`. The device must first be registered against `POST /api/devices/register` to obtain `DEVICE_API_KEY`; `TelemetrySender` then authenticates every request with that key (validated server-side by `DeviceApiKeyAuthFilter`).

## Secrets

`secrets.h` is gitignored. Copy `secrets.h.example` and provide:

- Wi-Fi SSID/password
- `BACKEND_BASE_URL` — the deployed (or local) backend URL
- `DEVICE_API_KEY` — issued by the device-registration endpoint

## Building and flashing

See `hardware/ESP32_Firmware_Command_Guide.txt` for the full PlatformIO CLI reference. The essentials:

```sh
cd hardware
pio run                       # compile
pio run --target upload       # compile + flash over USB
pio device monitor --baud 115200   # view Serial output
```
