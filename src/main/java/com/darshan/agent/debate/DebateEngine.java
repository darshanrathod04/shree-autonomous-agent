package com.darshan.agent.debate;

import org.springframework.stereotype.Component;

@Component
public class DebateEngine {

    private final ResearchAgent researcher;
    private final CriticAgent critic;
    private final RefinerAgent refiner;
    private final DebateMemory memory;
    private final ProposerAgent proposer;
    private final JudgeAgent judge;

    public DebateEngine(
            ResearchAgent researcher,
            CriticAgent critic,
            RefinerAgent refiner,
            ProposerAgent proposer,
            JudgeAgent judge,
            DebateMemory memory) {

        this.researcher = researcher;
        this.critic = critic;
        this.refiner = refiner;
        this.proposer = proposer;
        this.judge = judge;
        this.memory = memory;
    }

    public String debate(String problem) {

        memory.clear();

        // =========================
        // 1️⃣ Research Phase
        // =========================
        String research =
                researcher.propose(problem);

        memory.add("Research", research);

        // =========================
        // 2️⃣ Initial Proposal
        // =========================
        String proposal =
                proposer.propose(problem, research);

        DebateState state =
                new DebateState(proposal);

        memory.add("Proposal", proposal);

        // =========================
        // 🔁 Recursive Debate Loop
        // =========================
        for (int i = 0; i < 3; i++) {

            System.out.println("🧠 Debate Round " + (i + 1));

            // Critique
            String critique =
                    critic.critique(state.getProposal());

            memory.add("Critique-" + i, critique);
            state.setCritique(critique);

            // Refinement
            String improved =
                    refiner.refine(
                            state.getProposal(),
                            critique);

            memory.add("Refined-" + i, improved);
            state.setProposal(improved);

            // Judge decision
            if (judge.isGoodEnough(improved)) {
                System.out.println("⚖️ Judge accepted answer");
                break;
            }

            state.nextRound();
        }

        memory.add("Final", state.getProposal());

        return state.getProposal();
    }
}
