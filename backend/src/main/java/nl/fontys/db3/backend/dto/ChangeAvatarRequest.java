package nl.fontys.db3.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeAvatarRequest(
        @NotBlank String avatarName
) {}
