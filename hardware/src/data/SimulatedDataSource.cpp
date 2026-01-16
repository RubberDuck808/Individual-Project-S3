#include "SimulatedDataSource.h"
#include "../model/Telemetry.h"

bool SimulatedDataSource::begin() {
  t = 0.0f;
  Serial.println("[SimulatedDataSource] Initialized (test data mode)");
  return true;
}

bool SimulatedDataSource::read(Telemetry& out) {
  out = Telemetry();
  out.tsMs = millis();

  t += 0.05f;
  out.speedKph = 50.0f + 20.0f * sinf(t);
  out.rpm = 1500.0f + 800.0f * sinf(t * 1.3f);
  out.throttlePct = 10.0f + 30.0f * (0.5f + 0.5f * sinf(t * 0.7f));
  
  out.coolantTempC = 85.0f + 10.0f * sinf(t * 0.3f);
  out.batteryVoltageV = 12.5f + 0.5f * sinf(t * 0.2f);
  out.oilTempC = 90.0f + 5.0f * sinf(t * 0.25f);
  out.fuelLevelPct = 75.0f - (t * 0.01f);
  out.intakeAirTempC = 25.0f + 5.0f * sinf(t * 0.4f);
  out.engineLoadPct = 40.0f + 20.0f * sinf(t * 0.6f);
  out.mafAirFlow = 15.0f + 5.0f * sinf(t * 0.5f);
  out.mapPressure = 50.0f + 10.0f * sinf(t * 0.45f);
  out.timingAdvance = 10.0f + 5.0f * sinf(t * 0.35f);
  
  if ((int)(t * 10) % 100 == 0) {
    out.diagnosticCodes = "P0301";
  }

  return true;
}
