#pragma once
#include <Arduino.h>

struct Telemetry {
  uint32_t tsMs;
  float speedKph;
  float rpm;
  float throttlePct;
};
