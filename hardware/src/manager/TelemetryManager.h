#pragma once
#include "../data/IDataSource.h"
#include "../sender/TelemetrySender.h"
#include "../config/Config.h"

/**
 * TelemetryManager - Orchestrates the data flow
 * 
 * Responsibilities:
 * - Reads data from data source (real or simulated)
 * - Manages timing for live vs history sends
 * - Coordinates between data source and sender
 * - Handles errors and retries
 * 
 * This is the main coordinator that connects data source -> sender
 */
class TelemetryManager {
public:
  TelemetryManager(IDataSource* dataSource, TelemetrySender* sender);
  
  /**
   * Initialize the manager
   */
  bool begin();
  
  /**
   * Process one iteration (call this in loop())
   * Handles timing and sends data as needed
   */
  void process();
  
  /**
   * Get statistics
   */
  unsigned long getLiveSendCount() const { return liveSendCount; }
  unsigned long getHistorySendCount() const { return historySendCount; }
  unsigned long getErrorCount() const { return errorCount; }

private:
  IDataSource* dataSource;
  TelemetrySender* sender;
  
  unsigned long lastLiveSend;
  unsigned long lastHistorySend;
  
  unsigned long liveSendCount;
  unsigned long historySendCount;
  unsigned long errorCount;
  
  /**
   * Read data from source and send if ready
   */
  void processLiveTelemetry();
  void processHistoryTelemetry();
};
