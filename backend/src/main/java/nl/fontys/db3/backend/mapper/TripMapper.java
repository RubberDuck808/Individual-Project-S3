package nl.fontys.db3.backend.mapper;

import nl.fontys.db3.backend.dto.TripDTO;
import nl.fontys.db3.backend.entity.Trip;
import org.springframework.stereotype.Component;

@Component
public class TripMapper {

    public TripDTO toDTO(Trip trip) {
        if (trip == null) return null;

        return TripDTO.builder()
                .id(trip.getId())
                .userId(trip.getUser().getId())
                .convoyId(trip.getConvoyId())
                .startLat(trip.getStartLat())
                .startLng(trip.getStartLng())
                .endLat(trip.getEndLat())
                .endLng(trip.getEndLng())
                .distanceKm(trip.getDistanceKm())
                .startedAt(trip.getStartedAt())
                .endedAt(trip.getEndedAt())
                .createdAt(trip.getCreatedAt())
                .build();
    }
}
