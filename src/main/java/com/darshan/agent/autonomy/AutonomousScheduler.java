package com.darshan.agent.autonomy;

import com.darshan.agent.context.ContextStore;
import com.darshan.agent.context.ConversationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AutonomousScheduler {

    private final AutonomousLoop loop;
    private final ContextStore contextStore;

    public AutonomousScheduler(
            AutonomousLoop loop,
            ContextStore contextStore) {

        this.loop = loop;
        this.contextStore = contextStore;
    }

    private boolean running = false;

    @Scheduled(fixedDelay = 10000)
    public void think() {

        System.out.println("⏱ Autonomous tick...");
        try {
            ConversationContext context =
                    contextStore.getContext();

            String result = loop.run(context);

            if (result != null) {
                System.out.println("🤖 Autonomous Thought:");
                System.out.println(result);
            }

        } catch (Exception e) {
            System.out.println("Autonomous error: " + e.getMessage());
        } finally {
            running = false;
        }
    }
}