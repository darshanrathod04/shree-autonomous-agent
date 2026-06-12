package com.darshan.agent.debate.swarm;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdaptiveSwarmSelector {

    private final AgentPerformanceMemory memory;

    public AdaptiveSwarmSelector(
            AgentPerformanceMemory memory) {
        this.memory = memory;
    }

    public List<SwarmWorkerAgent> rank(
            List<SwarmWorkerAgent> workers) {

        workers.sort((a, b) ->
                Double.compare(
                        memory.score(b.name()),
                        memory.score(a.name())
                ));

        return workers;
    }
}
