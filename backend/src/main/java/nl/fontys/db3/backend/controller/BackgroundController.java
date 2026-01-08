package nl.fontys.db3.backend.controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.BackgroundDTO;
import nl.fontys.db3.backend.service.BackgroundService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backgrounds")
@RequiredArgsConstructor
public class BackgroundController {

    private final BackgroundService backgroundService;

    @GetMapping
    public List<BackgroundDTO> getActiveBackgrounds() {
        return backgroundService.getActiveBackgrounds();
    }
}
