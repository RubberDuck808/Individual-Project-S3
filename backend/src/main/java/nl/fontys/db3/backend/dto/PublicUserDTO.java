package nl.fontys.db3.backend.dto;

import lombok.Builder;

@Builder
public record PublicUserDTO(
        String username,
        String name,
        String avatarName,
        String avatarUrl,
        String backgroundName,
        String backgroundUrl
) {}
