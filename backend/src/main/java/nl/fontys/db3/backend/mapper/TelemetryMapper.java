package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.TelemetryDTO;
import nl.fontys.db3.backend.dto.TelemetryRequestDTO;
import nl.fontys.db3.backend.entity.Telemetry;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TelemetryMapper {

    TelemetryDTO toDTO(Telemetry telemetry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    Telemetry toEntity(TelemetryRequestDTO dto);
}
