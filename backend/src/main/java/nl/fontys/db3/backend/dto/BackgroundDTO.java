package nl.fontys.db3.backend.dto;

import lombok.Builder;

@Builder
public record BackgroundDTO(
        String name,
        String imagePath,
        String url,
        boolean active
) {}
