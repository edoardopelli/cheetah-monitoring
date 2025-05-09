package org.cheetah.monitoring.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a lock for a scheduled job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "jobLocks")
public class JobLock {
    @Id
    private String id;
    private String jobName;   // e.g., "PortStatusJob"
    private String lockId;    // Generated UUID
    private long timestamp;   // Time (in millis) when the lock was created
}