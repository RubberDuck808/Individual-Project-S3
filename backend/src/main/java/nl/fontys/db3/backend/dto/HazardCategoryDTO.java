package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HazardCategoryDTO {
    private Long id;
    private String name;
}
