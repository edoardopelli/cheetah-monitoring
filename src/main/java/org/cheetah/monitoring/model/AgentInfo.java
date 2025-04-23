package org.cheetah.monitoring.model;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the registration information of an agent.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "agents")
public class AgentInfo {
	@Id
	private String id;

	private String hostname;
	private String ip;
	// List of open ports on the host
	private List<Integer> openPorts;
	// Timestamp of the registration (in millis)
	private long timestamp;
	// The port on which the agent is listening (randomly chosen at startup)
	private int agentPort;

	// The agent status managed by the server: "UP" or "DOWN"
	private String status;
}