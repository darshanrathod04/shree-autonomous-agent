package com.darshan.agent.society;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class AgentSociety {

    private final List<DynamicAgent> agents = new ArrayList<>();

    public DynamicAgent spawn(String role) {

        DynamicAgent agent =
                new DynamicAgent(
                        UUID.randomUUID().toString(),
                        role);

        agents.add(agent);

        System.out.println("🧬 Spawned agent: " + role);

        return agent;
    }

    public List<DynamicAgent> all() {
        return agents;
    }

    public List<DynamicAgent> bestAgents() {
        return agents.stream()
                .sorted((a,b) ->
                        Double.compare(b.score(), a.score()))
                .limit(3)
                .toList();
    }
}

