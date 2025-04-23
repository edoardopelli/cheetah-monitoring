package org.cheetah.monitoring.repositories;

import org.cheetah.monitoring.model.AgentInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface AgentInfoRepository extends MongoRepository<AgentInfo, String> {
    /**
     * Finds an agent by its ip and hostname.
     *
     * @param ip The agent's IP address.
     * @param hostname The agent's hostname.
     * @return The matching AgentInfo, or null if not found.
     */
    AgentInfo findByIpAndHostname(String ip, String hostname);
    
    /**
     * Finds an agent record by its hostname.
     */
    AgentInfo findByHostname(String hostname);
}