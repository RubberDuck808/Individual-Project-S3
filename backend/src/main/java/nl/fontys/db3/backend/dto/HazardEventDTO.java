package nl.fontys.db3.backend.dto;

public class HazardEventDTO {
    private String type; // "UPSERT" | "DELETE"
    private HazardReportDTO hazard;
    private Long hazardId;

    public HazardEventDTO() {}

    public static HazardEventDTO upsert(HazardReportDTO hazard) {
        HazardEventDTO e = new HazardEventDTO();
        e.type = "UPSERT";
        e.hazard = hazard;
        return e;
    }

    public static HazardEventDTO delete(Long hazardId) {
        HazardEventDTO e = new HazardEventDTO();
        e.type = "DELETE";
        e.hazardId = hazardId;
        return e;
    }

    public String getType() { return type; }
    public HazardReportDTO getHazard() { return hazard; }
    public Long getHazardId() { return hazardId; }

    public void setType(String type) { this.type = type; }
    public void setHazard(HazardReportDTO hazard) { this.hazard = hazard; }
    public void setHazardId(Long hazardId) { this.hazardId = hazardId; }
}
