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
    private String title;
    private String description;
    private Double latitude;
    private Double longitude;
    private String category;     // e.g. "Accident", "Speed camera"
    private String status;       // e.g. "OPEN", "CLOSED"
    private String createdBy;    // username of reporter
    private long upvotes;
    private long downvotes;
    private int score;           // computed: upvotes - downvotes
}
