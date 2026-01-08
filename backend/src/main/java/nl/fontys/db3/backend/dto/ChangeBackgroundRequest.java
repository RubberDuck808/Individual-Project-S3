package nl.fontys.db3.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeBackgroundRequest(
        @NotBlank String backgroundName
) {}
