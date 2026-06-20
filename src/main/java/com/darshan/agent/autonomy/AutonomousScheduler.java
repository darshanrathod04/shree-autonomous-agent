package com.darshan.agent.autonomy;

import com.darshan.agent.context.ContextStore;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class AutonomousScheduler {

    private static final Logger log = LoggerFactory.getLogger(AutonomousScheduler.class);

    private final AutonomousLoop loop;
    private final ContextStore contextStore;
    private final OllamaClient ollamaClient;
    private final boolean enabled;

    // When true, autonomous processing is paused to allow user requests through
    private static final AtomicBoolean paused = new AtomicBoolean(false);

    // Track if a scheduler thought is currently in-flight (blocked on Ollama)
    private static final AtomicBoolean thoughtInProgress = new AtomicBoolean(false);

    // Track overlap count (how many times scheduler tried to start while previous still running)
    private static final AtomicInteger overlapCount = new AtomicInteger(0);

    // Track the start time of current thought
    private static final AtomicLong currentThoughtStartMs = new AtomicLong(0);

    // Unique scheduler tick counter
    private static final AtomicInteger tickCounter = new AtomicInteger(0);

    public AutonomousScheduler(
            AutonomousLoop loop,
            ContextStore contextStore,
            OllamaClient ollamaClient,
            @Value("${shree.scheduler.enabled:true}") boolean enabled) {
        this.loop = loop;
        this.contextStore = contextStore;
        this.ollamaClient = ollamaClient;
        this.enabled = enabled;
        if (enabled) {
            log.info("[Autonomous] Scheduler is ENABLED — will run autonomous background processing");
        } else {
            log.info("[Autonomous] Scheduler is DISABLED — running in USER-ONLY MODE");
        }
    }

    /**
     * Pause autonomous processing (called when user request arrives).
     */
    public static void pause() {
        paused.set(true);
        log.info("[Autonomous] PAUSED by user request");
    }

    /**
     * Resume autonomous processing (called after user request completes).
     */
    public static void resume() {
        paused.set(false);
        log.info("[Autonomous] RESUMED after user request");
    }

    /**
     * Check if autonomous processing is paused.
     */
    public static boolean isPaused() {
        return paused.get();
    }

    /**
     * Check if a thought is currently in progress.
     */
    public static boolean isThoughtInProgress() {
        return thoughtInProgress.get();
    }

    /**
     * Get the number of detected overlaps.
     */
    public static int getOverlapCount() {
        return overlapCount.get();
    }

    /**
     * Get the current active Ollama request count for diagnostics.
     */
    public static int getActiveOllamaRequests() {
        return OllamaClient.getActiveRequestCount();
    }

    @Scheduled(fixedDelay = 15000)
    public void think() {
        // HARD DISABLE: If scheduler is disabled via config, bail out immediately
        if (!enabled) {
            return;
        }

        tickCounter.incrementAndGet();
        Thread.currentThread().setName("shree-autonomous-scheduler");

        // Skip if paused (user request in progress)
        if (paused.get()) {
            log.debug("[Autonomous] Skipping tick {} - paused for user request", tickCounter.get());
            return;
        }

        // CRITICAL FIX: Skip if previous thought is still running (Ollama blocked)
        if (thoughtInProgress.get()) {
            int overlap = overlapCount.incrementAndGet();
            long elapsed = System.currentTimeMillis() - currentThoughtStartMs.get();
            log.warn("[Autonomous] OVERLAP DETECTED! Tick #{} skipped because previous thought still running after {}ms (total overlaps: {})",
                    tickCounter.get(), elapsed, overlap);
            log.warn("[Autonomous] Active Ollama requests: {}", getActiveOllamaRequests());
            return;
        }

        log.info("[Autonomous] Tick #{} starting... Thought in progress: {}, Paused: {}, Active Ollama: {}",
                tickCounter.get(), thoughtInProgress.get(), paused.get(), getActiveOllamaRequests());

        thoughtInProgress.set(true);
        currentThoughtStartMs.set(System.currentTimeMillis());

        try {
            ConversationContext context = contextStore.getContext();
            String result = loop.run(context);

            if (result != null) {
                long elapsed = System.currentTimeMillis() - currentThoughtStartMs.get();
                log.info("[Autonomous] Thought completed in {}ms: {}", elapsed, result);
            }
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - currentThoughtStartMs.get();
            log.error("[Autonomous] Error after {}ms: {}", elapsed, e.getMessage(), e);
        } finally {
            thoughtInProgress.set(false);
            currentThoughtStartMs.set(0);
        }
    }
}