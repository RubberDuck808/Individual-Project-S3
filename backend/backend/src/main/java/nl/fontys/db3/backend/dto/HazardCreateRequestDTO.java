package nl.fontys.db3.backend.dto;

import lombok.Data;

@Data
public class HazardCreateRequestDTO {
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private Long categoryId;
    private Long createdByUserId;
}
