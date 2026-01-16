package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminUserDTO {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String roleName;
    private LocalDateTime createdAt;
    private boolean active; // Can be used for soft delete
    private Long avatarId;
    private String avatarName;
    private Long backgroundId;
    private String backgroundName;
}
