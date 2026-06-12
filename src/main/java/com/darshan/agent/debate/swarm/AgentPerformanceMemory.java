package com.darshan.agent.debate.swarm;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class AgentPerformanceMemory {

    private final Map<String, Integer> success = new HashMap<>();
    private final Map<String, Integer> total = new HashMap<>();

    public void record(String agent, boolean good) {

        total.put(agent, total.getOrDefault(agent, 0) + 1);

        if (good) {
            success.put(agent,
                    success.getOrDefault(agent, 0) + 1);
        }
    }

    public double score(String agent) {

        int t = total.getOrDefault(agent, 1);
        int s = success.getOrDefault(agent, 0);

        return (double) s / t;
    }
}
