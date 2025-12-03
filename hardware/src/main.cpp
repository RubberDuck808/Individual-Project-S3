#include <Arduino.h>
#include "network.h"


void setup() {
    Serial.begin(115200);
    delay(1000);

    initWiFi();

    Serial.println("System initialized.");
}

void loop() {       // LED blinks every second
    Serial.println("hello");
    delay(1000);
}
