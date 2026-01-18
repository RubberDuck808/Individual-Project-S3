package nl.fontys.db3.backend.controller;

import lombok.RequiredArgsConstructor;
import nl.fontys.db3.backend.dto.AdminStatisticsDTO;
import nl.fontys.db3.backend.dto.AdminUserDTO;
import nl.fontys.db3.backend.service.AdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * GET /api/admin/statistics
     * Get system-wide statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<AdminStatisticsDTO> getStatistics() {
        AdminStatisticsDTO stats = adminService.getStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * GET /api/admin/users
     * Get all users (paginated)
     */
    @GetMapping("/users")
    public ResponseEntity<Page<AdminUserDTO>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<AdminUserDTO> users = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/admin/users/{id}
     * Get user by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<AdminUserDTO> getUserById(@PathVariable Long id) {
        AdminUserDTO user = adminService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    /**
     * PUT /api/admin/users/{id}/role
     * Update user role
     */
    @PutMapping("/users/{id}/role")
    public ResponseEntity<AdminUserDTO> updateUserRole(
            @PathVariable Long id,
            @RequestParam String roleName) {
        AdminUserDTO user = adminService.updateUserRole(id, roleName);
        return ResponseEntity.ok(user);
    }

    /**
     * DELETE /api/admin/users/{id}
     * Deactivate user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        adminService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }
}
