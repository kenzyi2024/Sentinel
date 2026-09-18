package com.sentinel.api;

import com.sentinel.domain.Agent;
import com.sentinel.simulation.AgentFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only API exposing the built-in agents and their capability grants.
 */
@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentFactory agentFactory;

    public AgentController(AgentFactory agentFactory) {
        this.agentFactory = agentFactory;
    }

    @GetMapping
    public List<Agent> list() {
        return agentFactory.all();
    }
}
