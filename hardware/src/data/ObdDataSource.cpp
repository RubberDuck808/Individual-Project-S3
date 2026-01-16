#include "ObdDataSource.h"

bool ObdDataSource::begin() {
  Serial.println("[ObdDataSource] Initializing OBD-II connection...");
  Serial.println("[ObdDataSource] OBD-II initialized (placeholder)");
  return true;
}

bool ObdDataSource::read(Telemetry& out) {
  Serial.println("[ObdDataSource] WARNING: Real OBD data not yet implemented");
  return false;
}
