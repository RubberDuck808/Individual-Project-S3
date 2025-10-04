package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.entity.HazardReport;
import nl.fontys.db3.backend.entity.User;
import nl.fontys.db3.backend.entity.VoteType;
import nl.fontys.db3.backend.service.HazardReportService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/hazards")
public class HazardReportController {
    private final HazardReportService hazardService;

    public HazardReportController(HazardReportService hazardService) {
        this.hazardService = hazardService;
    }

    @GetMapping("/open")
    public List<HazardReport> getOpenHazards() {
        return hazardService.getAllOpenHazards();
    }

    @PostMapping
    public HazardReport create(@RequestBody HazardReport hazard) {
        return hazardService.createHazard(hazard);
    }

    @PostMapping("/{id}/vote")
    public HazardReport vote(
            @PathVariable Long id,
            @RequestParam Long userId,
            @RequestParam VoteType type
    ) {
        User dummyUser = new User(); // TODO: fetch real user via repo/auth
        dummyUser.setId(userId);
        return hazardService.addVote(id, dummyUser, type);
    }
}

