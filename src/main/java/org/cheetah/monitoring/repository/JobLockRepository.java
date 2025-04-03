package org.cheetah.monitoring.repository;

import org.cheetah.monitoring.model.JobLock;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobLockRepository extends MongoRepository<JobLock, String> {
    JobLock findByJobName(String jobName);
}