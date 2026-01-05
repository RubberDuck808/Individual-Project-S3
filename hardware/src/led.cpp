#include <Arduino.h>
#include "led.h"

const int LED_PIN = 2; // Most ESP32 boards

void initLED() {
    pinMode(LED_PIN, OUTPUT);
}

void blinkLED() {
    digitalWrite(LED_PIN, HIGH);
    delay(500);
    digitalWrite(LED_PIN, LOW);
    delay(500);
}
