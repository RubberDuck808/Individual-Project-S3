package nl.fontys.db3.backend.controller;

import nl.fontys.db3.backend.dto.HazardCreateRequestDTO;
import nl.fontys.db3.backend.dto.HazardReportDTO;
import nl.fontys.db3.backend.mapper.HazardMapper;
import nl.fontys.db3.backend.service.hazard.HazardCommandService;
import nl.fontys.db3.backend.service.hazard.HazardQueryService;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequestMapping("/api/hazards")
public class HazardReportController {

    private final HazardQueryService queryService;
    private final HazardCommandService commandService;
    private final HazardMapper hazardMapper;

    public HazardReportController(
            HazardQueryService queryService,
            HazardCommandService commandService,
            HazardMapper hazardMapper
    ) {
        this.queryService = queryService;
        this.commandService = commandService;
        this.hazardMapper = hazardMapper;
    }

    /** GET all open hazards */
    @GetMapping("/open")
    public List<HazardReportDTO> getOpenHazards() {
        return hazardMapper.toDTOList(queryService.getOpenHazards());
    }

    /** POST create new hazard */
    @PostMapping
    public HazardReportDTO create(@RequestBody HazardCreateRequestDTO dto) {
        return hazardMapper.toDTO(commandService.createHazard(dto));
    }
}
