package nl.fontys.db3.backend.controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AvatarDTO;
import nl.fontys.db3.backend.service.AvatarService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/avatars")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @GetMapping
    public List<AvatarDTO> getActiveAvatars() {
        return avatarService.getActiveAvatars();
    }
}
