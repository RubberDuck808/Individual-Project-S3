package nl.fontys.db3.backend.service;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.TripCompleteRequestDTO;
import nl.fontys.db3.backend.dto.TripDTO;
import nl.fontys.db3.backend.entity.Trip;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.mapper.TripMapper;
import nl.fontys.db3.backend.repository.TripRepository;
import nl.fontys.db3.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TripService {

    private final TripRepository tripRepo;
    private final UserRepository userRepo;
    private final StatisticsService statisticsService;
    private final TripMapper tripMapper;

    @Transactional
    public TripDTO completeSoloTrip(String userEmail, TripCompleteRequestDTO dto) {
        if (dto == null) throw new IllegalArgumentException("Body cannot be null");
        require(dto.getStartLat(), "startLat");
        require(dto.getStartLng(), "startLng");
        require(dto.getEndLat(), "endLat");
        require(dto.getEndLng(), "endLng");
        require(dto.getDistanceKm(), "distanceKm");
        if (dto.getDistanceKm() < 0) throw new IllegalArgumentException("distanceKm cannot be negative");

        if (dto.getStartedAt() == null) throw new IllegalArgumentException("startedAt cannot be null");
        if (dto.getEndedAt() == null) throw new IllegalArgumentException("endedAt cannot be null");
        if (dto.getEndedAt().isBefore(dto.getStartedAt())) {
            throw new IllegalArgumentException("endedAt cannot be before startedAt");
        }

        User user = userRepo.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Trip trip = Trip.builder()
                .user(user)
                // convoyId left null for solo trips
                .startLat(dto.getStartLat())
                .startLng(dto.getStartLng())
                .endLat(dto.getEndLat())
                .endLng(dto.getEndLng())
                .distanceKm(dto.getDistanceKm())
                .startedAt(dto.getStartedAt())
                .endedAt(dto.getEndedAt())
                .build();

        Trip saved = tripRepo.save(trip);

        statisticsService.incrementTripsAndDistance(user.getId(), dto.getDistanceKm());

        return tripMapper.toDTO(saved);
    }

    private void require(Double v, String name) {
        if (v == null) throw new IllegalArgumentException(name + " cannot be null");
    }
}
