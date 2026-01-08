package nl.fontys.db3.backend.dto;

public record HazardCategoryDTO(
        Long id,
        String name,
        String iconUrl,
        boolean active
) {}
