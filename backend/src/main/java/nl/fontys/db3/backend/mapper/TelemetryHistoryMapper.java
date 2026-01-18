package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.TelemetryHistoryDTO;
import nl.fontys.db3.backend.dto.TelemetryHistoryRequestDTO;
import nl.fontys.db3.backend.entity.TelemetryHistory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TelemetryHistoryMapper {

    TelemetryHistoryDTO toDTO(TelemetryHistory history);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    TelemetryHistory toEntity(TelemetryHistoryRequestDTO dto);
}
