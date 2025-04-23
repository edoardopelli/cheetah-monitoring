package org.cheetah.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a threshold configuration for a specific metric.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "thresholds")
public class Threshold {
    @Id
    private String id;
    
    // For example: "CPU", "Disk", "RAM", "PORT"
    private String metricType;
    
    // The threshold value (percentage for CPU, Disk, RAM, etc.)
    private double thresholdValue;
}