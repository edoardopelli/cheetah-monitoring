package org.cheetah.monitoring.repositories;

import org.cheetah.monitoring.model.JobLock;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JobLockRepository extends MongoRepository<JobLock, String> {
    JobLock findByJobName(String jobName);
}