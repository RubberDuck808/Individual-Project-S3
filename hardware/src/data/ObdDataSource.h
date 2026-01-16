#pragma once
#include "IDataSource.h"

// Real OBD-II data source
class ObdDataSource : public IDataSource {
public:
  bool begin() override;
  bool read(Telemetry& out) override;

private:
  // TODO: Add OBD-II communication here
};
