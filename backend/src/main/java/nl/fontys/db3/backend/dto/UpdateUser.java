package nl.fontys.db3.backend.dto;

import lombok.Data;

@Data
public class UpdateUser {

    // profile info
    private String name;
    private String username;
    private String email;

    // password change
    private String currentPassword;
    private String newPassword;
}
