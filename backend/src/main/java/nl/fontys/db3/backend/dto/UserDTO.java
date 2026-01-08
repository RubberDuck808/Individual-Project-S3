package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private String username;
    private String name;
    private String email;
    private String roleName;
    private String createdAt;

    private String avatarName;
    private String avatarUrl;
    
    private String backgroundName;
    private String backgroundUrl;  

}

