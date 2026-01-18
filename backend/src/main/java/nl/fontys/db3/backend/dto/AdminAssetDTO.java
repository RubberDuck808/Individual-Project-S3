package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAssetDTO {
    private Long id;
    private String name;
    private String imagePath;
    private String url;
    private boolean active;
    private Long usageCount; // How many users are using this asset
}
