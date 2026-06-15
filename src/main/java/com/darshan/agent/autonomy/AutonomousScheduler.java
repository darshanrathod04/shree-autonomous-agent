package com.darshan.agent.autonomy;

import com.darshan.agent.context.ContextStore;
import com.darshan.agent.context.ConversationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class AutonomousScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutonomousScheduler.class);

    private final AutonomousLoop loop;
    private final ContextStore contextStore;

    // When true, autonomous processing is paused to allow user requests through
    private static final AtomicBoolean paused = new AtomicBoolean(false);

    public AutonomousScheduler(
            AutonomousLoop loop,
            ContextStore contextStore) {
        this.loop = loop;
        this.contextStore = contextStore;
    }

    /**
     * Pause autonomous processing (called when user request arrives).
     */
    public static void pause() {
        paused.set(true);
    }

    /**
     * Resume autonomous processing (called after user request completes).
     */
    public static void resume() {
        paused.set(false);
    }

    /**
     * Check if autonomous processing is paused.
     */
    public static boolean isPaused() {
        return paused.get();
    }

    @Scheduled(fixedDelay = 15000)  // Increased from 10s to 15s
    public void think() {
        // Skip autonomous tick if paused (user request in progress)
        if (paused.get()) {
            log.debug("[Autonomous] Skipping tick - paused for user request");
            return;
        }

        log.info("[Autonomous] Tick...");
        try {
            ConversationContext context = contextStore.getContext();
            String result = loop.run(context);

            if (result != null) {
                log.info("[Autonomous] Thought: {}", result);
            }
        } catch (Exception e) {
            log.error("[Autonomous] Error: {}", e.getMessage());
        }
    }
}