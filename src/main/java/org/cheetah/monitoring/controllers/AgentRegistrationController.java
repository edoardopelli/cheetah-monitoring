package org.cheetah.monitoring.controllers;

import org.cheetah.monitoring.model.AgentInfo;
import org.cheetah.monitoring.service.AgentRegistrationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/api/agent")
@AllArgsConstructor
public class AgentRegistrationController {

    private final AgentRegistrationService agentRegistrationService;


    /**
     * Registers or updates an agent.
     *
     * @param agentInfo The registration data sent by the agent.
     * @return The saved or updated AgentInfo object.
     */
    @PostMapping("/register")
    public AgentInfo registerAgent(@RequestBody AgentInfo agentInfo) {
        return agentRegistrationService.registerAgent(agentInfo);
    }
}