package com.darshan.agent.debate.swarm;

import com.darshan.agent.debate.CriticAgent;
import com.darshan.agent.debate.RefinerAgent;
import com.darshan.agent.society.AgentSociety;
import com.darshan.agent.society.DynamicAgent;
import com.darshan.agent.society.EvolutionEngine;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Component
public class DebateSwarmEngine {

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
    // 🧠 PARALLEL SWARM THINKING
    // ===============================
    public String swarmThink(String problem) {

        evolution.evolve(problem);


        List<SwarmWorkerAgent> ranked =
                selector.rank(workers);

        List<SwarmResult> results = new ArrayList<>();

        for (SwarmWorkerAgent w : ranked) {

            String answer = w.solve(problem);

            results.add(
                    new SwarmResult(w.name(), answer)
            );
        }

        String best = judge.chooseBest(results);

        // 🧠 LEARNING STEP
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


        return best;
    }

}
