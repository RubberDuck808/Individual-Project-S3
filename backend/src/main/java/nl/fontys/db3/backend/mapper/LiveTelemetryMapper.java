package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.LiveTelemetryDTO;
import nl.fontys.db3.backend.dto.LiveTelemetryRequestDTO;
import nl.fontys.db3.backend.entity.LiveTelemetry;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface LiveTelemetryMapper {

    LiveTelemetryDTO toDTO(LiveTelemetry liveTelemetry);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", expression = "java(java.time.Instant.now())")
    LiveTelemetry toEntity(LiveTelemetryRequestDTO dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastUpdated", expression = "java(java.time.Instant.now())")
    void updateEntity(LiveTelemetryRequestDTO dto, @MappingTarget LiveTelemetry entity);
}
