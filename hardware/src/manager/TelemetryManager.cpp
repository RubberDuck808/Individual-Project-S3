#include "TelemetryManager.h"
#include "../model/Telemetry.h"

TelemetryManager::TelemetryManager(IDataSource* dataSource, TelemetrySender* sender)
  : dataSource(dataSource), sender(sender),
    lastLiveSend(0), lastHistorySend(0),
    liveSendCount(0), historySendCount(0), errorCount(0) {
}

bool TelemetryManager::begin() {
  if (!dataSource) {
    Serial.println("[TelemetryManager] ERROR: Data source is null");
    return false;
  }
  
  if (!sender) {
    Serial.println("[TelemetryManager] ERROR: Sender is null");
    return false;
  }
  
  if (!dataSource->begin()) {
    Serial.println("[TelemetryManager] ERROR: Data source failed to initialize");
    return false;
  }
  
  if (!sender->isReady()) {
    Serial.println("[TelemetryManager] WARNING: Sender not ready (WiFi may not be connected)");
    // Don't fail here, WiFi might connect later
  }
  
  Serial.println("[TelemetryManager] Initialized");
  Serial.printf("  Live interval: %lu ms\n", LIVE_TELEMETRY_INTERVAL_MS);
  Serial.printf("  History interval: %lu ms\n", HISTORY_TELEMETRY_INTERVAL_MS);
  
  return true;
}

void TelemetryManager::process() {
  unsigned long now = millis();
  
  if (now - lastLiveSend >= LIVE_TELEMETRY_INTERVAL_MS) {
    processLiveTelemetry();
    lastLiveSend = now;
  }
  
  if (now - lastHistorySend >= HISTORY_TELEMETRY_INTERVAL_MS) {
    processHistoryTelemetry();
    lastHistorySend = now;
  }
}

void TelemetryManager::processLiveTelemetry() {
  Telemetry t;
  
  if (!dataSource->read(t)) {
    Serial.println("[TelemetryManager] Failed to read from data source");
    errorCount++;
    return;
  }
  
  t.tsMs = millis();
  
  if (sender->sendLive(t)) {
    liveSendCount++;
  } else {
    errorCount++;
  }
}

void TelemetryManager::processHistoryTelemetry() {
  Telemetry t;
  
  if (!dataSource->read(t)) {
    Serial.println("[TelemetryManager] Failed to read from data source");
    errorCount++;
    return;
  }
  
  t.tsMs = millis();
  
  if (sender->sendHistory(t)) {
    historySendCount++;
  } else {
    errorCount++;
  }
}
