package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.TripCompleteRequestDTO;
import nl.fontys.db3.backend.dto.TripDTO;
import nl.fontys.db3.backend.service.TripService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trips")
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @PostMapping("/complete")
    public ResponseEntity<TripDTO> completeTrip(
            @RequestBody TripCompleteRequestDTO dto,
            Authentication authentication
    ) {
        String email = authentication.getName();
        TripDTO result = tripService.completeSoloTrip(email, dto);
        return ResponseEntity.ok(result);
    }
}
