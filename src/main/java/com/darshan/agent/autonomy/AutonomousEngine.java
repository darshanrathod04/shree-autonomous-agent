package com.darshan.agent.autonomy;

import com.darshan.agent.context.ContextStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutonomousEngine {

    private final AutonomousLoop loop;
    private final ContextStore contextStore;

    public AutonomousEngine(
            AutonomousLoop loop,
            ContextStore contextStore) {

        this.loop = loop;
        this.contextStore = contextStore;
    }

    // runs every 5 seconds
    @Scheduled(fixedDelay = 5000)
    public void think() throws Exception {

        loop.run(contextStore.getContext());
    }
}
