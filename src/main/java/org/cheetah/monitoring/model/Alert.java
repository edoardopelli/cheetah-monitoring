package org.cheetah.monitoring.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents an alert sent for a specific metric on a host.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "alerts")
public class Alert {
    @Id
    private String id;
    
    private String hostname;
    private String ip;          // Aggiunto per differenziare per ip
    private String metricType;  // e.g., "CPU", "Disk", "RAM"
    private long timestamp;     // Time (in millis) when the alert was sent
}