package com.darshan.agent.cognition;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.EpisodicRecallEngine;
import com.darshan.agent.state.AgentState;
import org.springframework.stereotype.Component;

@Component
public class CognitiveGovernorEngine {

    private final EpisodicRecallEngine recall;
    private final AgentState state;

    public CognitiveGovernorEngine(EpisodicRecallEngine recall,
                                   AgentState state) {
        this.recall = recall;
        this.state = state;
    }

    /**
     * Main cognitive control entry point
     */
    public CognitiveDecision evaluate(String input, ConversationContext context) {

        input = input.toLowerCase();
        String text = input.toLowerCase();

        // safety refusal
        if (text.contains("illegal")
                || text.contains("harmful")) {

            return new CognitiveDecision(
                    CognitiveDecision.Action.REFUSE,
                    "Unsafe request");
        }

        // overload protection
        if (context.getWorkingMemory() != null
                && context.getWorkingMemory().length() > 4000) {

            return new CognitiveDecision(
                    CognitiveDecision.Action.PAUSE_AGENT,
                    "Cognitive overload");
        }

        // ---- 1. Cognitive health check ----
        if (state.getFatigue() > 0.7 ||
                state.getConfidence() < 0.1) {

            return new CognitiveDecision(
                    CognitiveDecision.Action.PAUSE_AGENT,
                    "Cognitive exhaustion detected");
        }

        // ---- 2. Safety rule ----
        if (isHarmful(input)) {
            return new CognitiveDecision(
                    CognitiveDecision.Action.REFUSE,
                    "Unsafe request");
        }

        // ---- 3. Tool detection ----
        if (requiresLiveData(input)) {
            return new CognitiveDecision(
                    CognitiveDecision.Action.USE_TOOL,
                    "Live data required");
        }

        // ---- 4. Memory recall trigger ----
        if (needsMemory(input)) {
            return new CognitiveDecision(
                    CognitiveDecision.Action.RECALL_AND_RESPOND,
                    "Memory relevant");
        }

        // ---- 5. Ambiguity detection ----
        if (input.length() < 3) {
            return new CognitiveDecision(
                    CognitiveDecision.Action.ASK_CLARIFICATION,
                    "Input unclear");
        }

        return new CognitiveDecision(
                CognitiveDecision.Action.RESPOND,
                "Normal response");
    }

    // ---------------- RULES ----------------

    private boolean needsMemory(String input) {
        return input.contains("maine")
                || input.contains("remember")
                || input.contains("about me")
                || input.contains("kya bataya");
    }

    private boolean requiresLiveData(String input) {
        return input.contains("latest")
                || input.contains("live")
                || input.contains("current")
                || input.contains("price")
                || input.contains("market");
    }

    private boolean isHarmful(String input) {
        return input.contains("illegal")
                || input.contains("hack")
                || input.contains("harm");
    }
}