package nl.fontys.db3.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HazardReportDTO {
    private Long id;
    private Double latitude;
    private Double longitude;
    private String category;
    private String status;
    private Long createdBy;  // always user ID
    private long upvotes;
    private long downvotes;
}

