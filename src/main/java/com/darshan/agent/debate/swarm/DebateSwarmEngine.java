  package com.darshan.agent.debate.swarm;

import com.darshan.agent.debate.CriticAgent;
import com.darshan.agent.debate.RefinerAgent;
import com.darshan.agent.society.AgentSociety;
import com.darshan.agent.society.DynamicAgent;
import com.darshan.agent.society.EvolutionEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class DebateSwarmEngine {

    private static final Logger log = LoggerFactory.getLogger(DebateSwarmEngine.class);

    private final List<SwarmWorkerAgent> workers;
    private final CriticAgent critic;
    private final RefinerAgent refiner;
    private final SwarmJudge judge;
    private final AdaptiveSwarmSelector selector;
    private final AgentPerformanceMemory performance;
    private final EvolutionEngine evolution;
    private final AgentSociety society;

    private final ExecutorService pool =
            Executors.newFixedThreadPool(3);

    public DebateSwarmEngine(
            List<SwarmWorkerAgent> workers,
            CriticAgent critic,
            RefinerAgent refiner,
            SwarmJudge judge,
            AdaptiveSwarmSelector selector,
            AgentPerformanceMemory performance,
            EvolutionEngine evolution,
            AgentSociety society
    ) {
        this.workers = workers;
        this.critic = critic;
        this.refiner = refiner;
        this.judge = judge;
        this.selector = selector;
        this.performance = performance;
        this.evolution = evolution;
        this.society = society;
    }

    // ===============================
    // PARALLEL SWARM THINKING
    // ===============================
    public String swarmThink(String problem) {
        long start = System.currentTimeMillis();
        log.info("[Swarm] Starting swarm think, workers={}", workers.size());

        evolution.evolve(problem);

        List<SwarmWorkerAgent> ranked = selector.rank(workers);

        // Run workers IN PARALLEL instead of sequentially
        List<Future<SwarmResult>> futures = new ArrayList<>();
        for (SwarmWorkerAgent w : ranked) {
            futures.add(pool.submit(() -> {
                long workerStart = System.currentTimeMillis();
                String answer = w.solve(problem);
                long workerElapsed = System.currentTimeMillis() - workerStart;
                log.info("[Swarm] Worker {} completed in {}ms", w.name(), workerElapsed);
                return new SwarmResult(w.name(), answer);
            }));
        }

        List<SwarmResult> results = new ArrayList<>();
        for (Future<SwarmResult> f : futures) {
            try {
                results.add(f.get(80, TimeUnit.SECONDS));
            } catch (Exception e) {
                log.error("[Swarm] Worker failed: {}", e.getMessage());
            }
        }

        String best = judge.chooseBest(results);

        // Learning step
        for (SwarmResult r : results) {
            boolean good = best.equals(r.answer());
            performance.record(r.role(), good);
        }
        for (DynamicAgent a : society.all()) {
            if (best.contains(a.role())) {
                a.reward();
            } else {
                a.punish();
            }
        }

        long elapsed = System.currentTimeMillis() - start;
        log.info("[Swarm] Swarm think completed in {}ms", elapsed);

        return best;
    }
}