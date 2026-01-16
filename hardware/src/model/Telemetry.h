#pragma once
#include <Arduino.h>

/**
 * Telemetry data model
 * Matches the backend DTOs for live and history telemetry
 */
struct Telemetry {
  // Timestamp
  uint32_t tsMs;
  
  // Basic engine data (always collected)
  float speedKph;
  float rpm;
  float throttlePct;
  
  // Extended OBD data (optional, collected when available)
  float coolantTempC;      // Engine coolant temperature
  float batteryVoltageV;   // Battery/control module voltage
  float oilTempC;          // Engine oil temperature
  float fuelLevelPct;      // Fuel tank level percentage
  float intakeAirTempC;    // Intake air temperature
  float engineLoadPct;     // Calculated engine load
  float mafAirFlow;        // Mass air flow rate (g/s)
  float mapPressure;       // Manifold absolute pressure (kPa)
  float timingAdvance;     // Timing advance (degrees)
  
  // Diagnostic codes (comma-separated string, e.g., "P0301,P0420")
  String diagnosticCodes;
  
  // Constructor to initialize all fields
  Telemetry() 
    : tsMs(0),
      speedKph(0.0f), rpm(0.0f), throttlePct(0.0f),
      coolantTempC(0.0f), batteryVoltageV(0.0f), oilTempC(0.0f),
      fuelLevelPct(0.0f), intakeAirTempC(0.0f), engineLoadPct(0.0f),
      mafAirFlow(0.0f), mapPressure(0.0f), timingAdvance(0.0f),
      diagnosticCodes("") {}
};
