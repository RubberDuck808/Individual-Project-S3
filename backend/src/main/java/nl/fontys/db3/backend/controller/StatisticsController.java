package nl.fontys.db3.backend.controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.UserStatsDTO;
import nl.fontys.db3.backend.service.StatisticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class StatisticsController {

    private final StatisticsService statisticsService;

    // GET /api/users/{username}/stats
    @GetMapping("/{username}/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@PathVariable String username) {
        return ResponseEntity.ok(statisticsService.getStatsByUsername(username));
    }
}
