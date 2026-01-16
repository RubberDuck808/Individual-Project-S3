#pragma once
#include "IDataSource.h"
#include "../config/Config.h"

#if DATA_SOURCE_MODE == DATA_SOURCE_SIMULATED
  #include "SimulatedDataSource.h"
#elif DATA_SOURCE_MODE == DATA_SOURCE_REAL
  #include "ObdDataSource.h"
#else
  #error "Invalid DATA_SOURCE_MODE. Use DATA_SOURCE_SIMULATED or DATA_SOURCE_REAL"
#endif

#include <Arduino.h>

/**
 * Factory for creating the appropriate data source based on configuration
 * This decouples the main code from the specific data source implementation
 */
class DataSourceFactory {
public:
  static IDataSource* create() {
    #if DATA_SOURCE_MODE == DATA_SOURCE_SIMULATED
      Serial.println("[DataSourceFactory] Creating SimulatedDataSource");
      return new SimulatedDataSource();
    #elif DATA_SOURCE_MODE == DATA_SOURCE_REAL
      Serial.println("[DataSourceFactory] Creating ObdDataSource");
      return new ObdDataSource();
    #endif
  }
  
  static const char* getModeName() {
    #if DATA_SOURCE_MODE == DATA_SOURCE_SIMULATED
      return "SIMULATED";
    #elif DATA_SOURCE_MODE == DATA_SOURCE_REAL
      return "REAL";
    #endif
  }
};
