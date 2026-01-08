#pragma once
#include "../model/Telemetry.h"

class IDataSource {
public:
  virtual ~IDataSource() = default;
  virtual bool begin() = 0;              // init hardware or sim
  virtual bool read(Telemetry& out) = 0; // get next sample
};
