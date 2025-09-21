package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.repository.TestMessageRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    private final TestMessageRepository repository;

    public HealthController(TestMessageRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/api/health")
    public String healthCheck() {
        try {
            long count = repository.count(); // run a simple DB query
            return "Database connection OK. Table has " + count + " rows.";
        } catch (Exception e) {
            return "Database connection FAILED: " + e.getMessage();
        }
    }
}
