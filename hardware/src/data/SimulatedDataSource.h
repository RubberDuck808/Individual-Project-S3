#pragma once
#include "IDataSource.h"

class SimulatedDataSource : public IDataSource {
public:
  bool begin() override;
  bool read(Telemetry& out) override;

private:
  float t = 0.0f;
};
