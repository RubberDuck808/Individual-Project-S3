#pragma once
#include <Arduino.h>
#include "../identity/DeviceIdentity.h"

/**
 * SerialCommands - Handle serial commands for device management
 * 
 * Commands:
 * - FACTORY_RESET: Clear device ID and restart
 * - GET_DEVICE_ID: Print current device ID
 * - HELP: Show available commands
 */
class SerialCommands {
public:
  /**
   * Check for and process serial commands
   * Call this in loop()
   */
  static void process();

private:
  static void handleFactoryReset();
  static void handleGetDeviceId();
  static void handleHelp();
};
