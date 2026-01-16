package nl.fontys.db3.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

/**
 * Historical telemetry data - stores all data points over time
 * Used for charts, analysis, and trends
 * Contains full OBD data
 */
@Entity
@Table(name = "telemetry_history", indexes = {
    @Index(name = "idx_device_timestamp", columnList = "device_id,timestamp")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", nullable = false)
    private String deviceId;

    @Column(nullable = false)
    private Instant timestamp;

    // Basic engine data (always collected)
    @Column(name = "speed_kph")
    private Double speedKph;
    
    @Column(name = "rpm")
    private Double rpm;
    
    @Column(name = "throttle_pct")
    private Double throttlePct;

    // Extended OBD data (collected when available)
    @Column(name = "coolant_temp_c")
    private Double coolantTempC;
    
    @Column(name = "battery_voltage_v")
    private Double batteryVoltageV;
    
    @Column(name = "oil_temp_c")
    private Double oilTempC;
    
    @Column(name = "fuel_level_pct")
    private Double fuelLevelPct;
    
    @Column(name = "intake_air_temp_c")
    private Double intakeAirTempC;
    
    @Column(name = "engine_load_pct")
    private Double engineLoadPct;
    
    @Column(name = "maf_air_flow")
    private Double mafAirFlow;
    
    @Column(name = "map_pressure")
    private Double mapPressure;
    
    @Column(name = "timing_advance")
    private Double timingAdvance;

    // Diagnostic codes
    @Column(name = "diagnostic_codes", length = 500)
    private String diagnosticCodes;
}
