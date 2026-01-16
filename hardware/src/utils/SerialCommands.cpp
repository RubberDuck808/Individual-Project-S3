#include "SerialCommands.h"
#include <esp_system.h>  // For ESP.restart()

void SerialCommands::process() {
  if (Serial.available()) {
    String cmd = Serial.readStringUntil('\n');
    cmd.trim();
    cmd.toUpperCase();
    
    if (cmd == "FACTORY_RESET") {
      handleFactoryReset();
    } else if (cmd == "GET_DEVICE_ID" || cmd == "ID") {
      handleGetDeviceId();
    } else if (cmd == "HELP" || cmd == "?") {
      handleHelp();
    } else if (cmd.length() > 0) {
      Serial.printf("Unknown command: %s\n", cmd.c_str());
      Serial.println("Type HELP for available commands");
    }
  }
}

void SerialCommands::handleFactoryReset() {
  Serial.println("\n========================================");
  Serial.println("FACTORY RESET");
  Serial.println("========================================");
  Serial.println("Clearing device ID...");
  
  DeviceIdentity::factoryReset();
  
  Serial.println("Device ID cleared!");
  Serial.println("Restarting in 2 seconds...");
  Serial.println("========================================\n");
  
  delay(2000);
  ESP.restart();
}

void SerialCommands::handleGetDeviceId() {
  String deviceId = DeviceIdentity::getDeviceId();
  Serial.println("\n========================================");
  Serial.println("DEVICE ID");
  Serial.println("========================================");
  Serial.printf("Current Device ID: %s\n", deviceId.c_str());
  Serial.println("========================================\n");
}

void SerialCommands::handleHelp() {
  Serial.println("\n========================================");
  Serial.println("AVAILABLE COMMANDS");
  Serial.println("========================================");
  Serial.println("FACTORY_RESET  - Clear device ID and restart");
  Serial.println("GET_DEVICE_ID  - Show current device ID");
  Serial.println("ID             - Alias for GET_DEVICE_ID");
  Serial.println("HELP           - Show this help message");
  Serial.println("?              - Alias for HELP");
  Serial.println("========================================\n");
}
