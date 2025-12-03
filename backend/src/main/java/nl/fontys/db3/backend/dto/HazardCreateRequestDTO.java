package nl.fontys.db3.backend.dto;

import lombok.Data;
import jakarta.validation.constraints.NotNull;


@Data
public class HazardCreateRequestDTO {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long createdBy;
}

