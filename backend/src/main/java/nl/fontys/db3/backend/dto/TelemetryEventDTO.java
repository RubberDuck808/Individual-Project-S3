package nl.fontys.db3.backend.dto;

public class TelemetryEventDTO {
    private String type; // "UPDATE"
    private String deviceId;
    private CarHealthDTO carHealth;

    public TelemetryEventDTO() {
        // Default constructor required for JSON deserialization
    }

    public static TelemetryEventDTO update(String deviceId, CarHealthDTO carHealth) {
        TelemetryEventDTO e = new TelemetryEventDTO();
        e.type = "UPDATE";
        e.deviceId = deviceId;
        e.carHealth = carHealth;
        return e;
    }

    public String getType() { return type; }
    public String getDeviceId() { return deviceId; }
    public CarHealthDTO getCarHealth() { return carHealth; }

    public void setType(String type) { this.type = type; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public void setCarHealth(CarHealthDTO carHealth) { this.carHealth = carHealth; }
}
