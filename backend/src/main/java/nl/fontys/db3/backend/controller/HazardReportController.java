package nl.fontys.db3.backend.controller;

import lombok.extern.slf4j.Slf4j;
import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.service.HazardService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/hazards")
public class HazardReportController {

    private final HazardService hazardService;
    private final HazardMapper hazardMapper;

    public HazardReportController(
            HazardService hazardService,
            HazardMapper hazardMapper
    ) {
        this.hazardService = hazardService;
        this.hazardMapper = hazardMapper;
    }

    @GetMapping("/open")
    public List<HazardReportDTO> getOpenHazards() {
        log.debug("Getting open hazards");
        try {
            List<HazardReportDTO> hazards = hazardMapper.toDTOList(hazardService.getOpenHazards());
            log.debug("Retrieved {} open hazards", hazards.size());
            return hazards;
        } catch (Exception e) {
            log.error("Error getting open hazards", e);
            throw e;
        }
    }

    @PostMapping
    public HazardReportDTO create(@Valid @RequestBody HazardCreateRequestDTO dto, Authentication authentication) {
        String email = authentication.getName();
        log.info("Creating hazard - email: {}, categoryId: {}, lat: {}, lng: {}", 
                email, dto.getCategoryId(), dto.getLatitude(), dto.getLongitude());
        try {
            HazardReportDTO created = hazardMapper.toDTO(hazardService.createHazard(dto, email));
            log.info("Hazard created successfully - hazardId: {}, email: {}", created.getId(), email);
            return created;
        } catch (Exception e) {
            log.error("Error creating hazard - email: {}", email, e);
            throw e;
        }
    }

    @GetMapping("/by-user/{username}")
    public List<HazardReportDTO> getHazardsByUser(@PathVariable String username) {
        log.debug("Getting hazards by user - username: {}", username);
        try {
            List<HazardReportDTO> hazards = hazardMapper.toDTOList(hazardService.getHazardsByUsername(username));
            log.debug("Retrieved {} hazards for user: {}", hazards.size(), username);
            return hazards;
        } catch (IllegalArgumentException e) {
            log.warn("Get hazards by user failed - username: {}, reason: {}", username, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error getting hazards by user - username: {}", username, e);
            throw e;
        }
    }
}
