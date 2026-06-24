package nl.fontys.db3.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.*;
import nl.fontys.db3.backend.service.AdminAssetService;
import nl.fontys.db3.backend.service.AvatarService;
import nl.fontys.db3.backend.service.BackgroundService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/assets")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAssetController {

    private final AdminAssetService adminAssetService;
    private final AvatarService avatarService;
    private final BackgroundService backgroundService;

    @GetMapping("/avatars")
    public ResponseEntity<List<AdminAssetDTO>> getAllAvatars() {
        List<AdminAssetDTO> avatars = adminAssetService.getAllAvatars();
        return ResponseEntity.ok(avatars);
    }

    @GetMapping("/avatars/{id}")
    public ResponseEntity<AvatarDTO> getAvatarById(@PathVariable Long id) {
        AvatarDTO avatar = avatarService.getAvatarById(id);
        return ResponseEntity.ok(avatar);
    }

    @PostMapping("/avatars")
    public ResponseEntity<AvatarDTO> createAvatar(@Valid @RequestBody CreateAvatarRequestDTO request) {
        AvatarDTO avatar = avatarService.createAvatar(request.getName(), request.getImagePath());
        return ResponseEntity.ok(avatar);
    }

    @PutMapping("/avatars/{id}")
    public ResponseEntity<AvatarDTO> updateAvatar(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssetRequestDTO request) {
        AvatarDTO avatar = avatarService.updateAvatar(
                id,
                request.getName(),
                request.getImagePath(),
                request.getActive()
        );
        return ResponseEntity.ok(avatar);
    }

    @DeleteMapping("/avatars/{id}")
    public ResponseEntity<Void> deleteAvatar(@PathVariable Long id) {
        avatarService.deleteAvatar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/avatars/{id}/deactivate")
    public ResponseEntity<Void> deactivateAvatar(@PathVariable Long id) {
        avatarService.deactivateAvatar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/backgrounds")
    public ResponseEntity<List<AdminAssetDTO>> getAllBackgrounds() {
        List<AdminAssetDTO> backgrounds = adminAssetService.getAllBackgrounds();
        return ResponseEntity.ok(backgrounds);
    }

    @GetMapping("/backgrounds/{id}")
    public ResponseEntity<BackgroundDTO> getBackgroundById(@PathVariable Long id) {
        BackgroundDTO background = backgroundService.getBackgroundById(id);
        return ResponseEntity.ok(background);
    }

    @PostMapping("/backgrounds")
    public ResponseEntity<BackgroundDTO> createBackground(@Valid @RequestBody CreateBackgroundRequestDTO request) {
        BackgroundDTO background = backgroundService.createBackground(request.getName(), request.getImagePath());
        return ResponseEntity.ok(background);
    }

    @PutMapping("/backgrounds/{id}")
    public ResponseEntity<BackgroundDTO> updateBackground(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAssetRequestDTO request) {
        BackgroundDTO background = backgroundService.updateBackground(
                id,
                request.getName(),
                request.getImagePath(),
                request.getActive()
        );
        return ResponseEntity.ok(background);
    }

    @DeleteMapping("/backgrounds/{id}")
    public ResponseEntity<Void> deleteBackground(@PathVariable Long id) {
        backgroundService.deleteBackground(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/backgrounds/{id}/deactivate")
    public ResponseEntity<Void> deactivateBackground(@PathVariable Long id) {
        backgroundService.deactivateBackground(id);
        return ResponseEntity.noContent().build();
    }
}
