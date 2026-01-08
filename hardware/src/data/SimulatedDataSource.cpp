#include "SimulatedDataSource.h"

bool SimulatedDataSource::begin() {
  t = 0.0f;
  return true;
}

bool SimulatedDataSource::read(Telemetry& out) {
  out.tsMs = millis();

  // simple “driving” waveform
  t += 0.05f;
  out.speedKph = 50.0f + 20.0f * sinf(t);
  out.rpm = 1500.0f + 800.0f * sinf(t * 1.3f);
  out.throttlePct = 10.0f + 30.0f * (0.5f + 0.5f * sinf(t * 0.7f));

  return true;
}
