package org.cheetah.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents system metrics collected from remote agents.
 */
@Data
@Document(collection = "metrics")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metrics {
    @Id
    private String id;
    
    private String hostname;
    private String ip;
    private long timestamp;
    
    // Usage values are expressed in percentage.
    private double cpuUsage;
    private double diskUsage;
    private double ramUsage;
    
    
}