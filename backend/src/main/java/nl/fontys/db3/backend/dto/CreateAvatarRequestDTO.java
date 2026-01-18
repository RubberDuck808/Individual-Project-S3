package nl.fontys.db3.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAvatarRequestDTO {
    @NotBlank
    private String name;
    
    @NotBlank
    private String imagePath; // Path in Google Cloud Storage
}
