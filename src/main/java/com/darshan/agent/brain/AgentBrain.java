package com.darshan.agent.brain;

import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.cognition.*;
import com.darshan.agent.context.*;
import com.darshan.agent.debate.swarm.DebateSwarmEngine;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.memory.EpisodicRecallEngine;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.planner.*;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Service;

@Service
public class AgentBrain {

    private final CognitiveGovernorEngine governor;
    private final ConversationStateMachine stateMachine;
    private final ContextStore contextStore;
    private final IntentEngine intentEngine;
    private final SkillRouter router;
    private final DebateSwarmEngine swarm;
    private final PersonalityEngine personality;
    private final EpisodicRecallEngine recall;
    private final MetaCognitionEngine meta;
    private final IdentityPerceptionEngine identityPerceptionEngine;

    public AgentBrain(
            CognitiveGovernorEngine governor,
            ConversationStateMachine stateMachine,
            ContextStore contextStore,
            IntentEngine intentEngine,
            SkillRouter router,
            DebateSwarmEngine swarm,
            PersonalityEngine personality,
            EpisodicRecallEngine recall,
            MetaCognitionEngine meta,
            IdentityPerceptionEngine identityPerceptionEngine
    ) {
        this.governor = governor;
        this.stateMachine = stateMachine;
        this.contextStore = contextStore;
        this.intentEngine = intentEngine;
        this.router = router;
        this.swarm = swarm;
        this.personality = personality;
        this.recall = recall;
        this.meta = meta;
        this.identityPerceptionEngine = identityPerceptionEngine;
    }

    // =====================================================
    // 🧠 MAIN COGNITIVE PIPELINE
    // =====================================================
    public AgentResponse process(
            String input,
            ConversationContext context
    ) throws Exception {

        if (context == null) {
            context = contextStore.getContext();
        }

        // -------------------------------------------------
        // 1️⃣ GOVERNOR (Safety + Stability)
        // -------------------------------------------------
        CognitiveDecision decision =
                governor.evaluate(input, context);

        switch (decision.getAction()) {

            case PAUSE_AGENT:
                return new AgentResponse(
                        "I need a short recovery pause.",
                        false);

            case REFUSE:
                return new AgentResponse(
                        "I cannot help with that request.",
                        false);

            case ASK_CLARIFICATION:
                return new AgentResponse(
                        "Can you clarify what you mean?",
                        false);
        }

        // -------------------------------------------------
        // 2️⃣ STATE MACHINE
        // -------------------------------------------------
        String stateReply =
                stateMachine.handle(input, context);

        if (stateReply != null) {
            return new AgentResponse(stateReply, false);
        }

        // -------------------------------------------------
        // 3️⃣ MEMORY RECALL (BEFORE THINKING)
        // -------------------------------------------------
        String recalledMemory =
                recall.recallRelevant(input);

        context.setWorkingMemory(recalledMemory);


        identityPerceptionEngine.perceive(input);

        // -------------------------------------------------
        // 4️⃣ INTENT DETECTION
        // -------------------------------------------------
        String intent =
                intentEngine.detectIntent(input);


        Skill skill = router.route(intent);

        String rawReply;

        // -------------------------------------------------
        // 5️⃣ SKILL EXECUTION
        // -------------------------------------------------
        if (skill != null) {
            rawReply = skill.execute(input, context);
        }
        // -------------------------------------------------
        // 6️⃣ REASONING FALLBACK
        // -------------------------------------------------
        else {
            rawReply = swarm.swarmThink(
                    input + "\nMemory:\n" + recalledMemory
            );
        }

        // -------------------------------------------------
        // 7️⃣ META COGNITION (SELF CHECK)
        // -------------------------------------------------
        MetaThought reflection =
                meta.evaluate(input, rawReply);

        if (!reflection.isSuccessful()) {
            rawReply =
                    rawReply +
                            "\n\n(Self-correction applied)";
        }

        // -------------------------------------------------
        // 8️⃣ PERSONALITY RENDER (LAST STEP ONLY)
        // -------------------------------------------------
        String finalReply =
                personality.applyPersonality(rawReply);

        return new AgentResponse(finalReply, true);
    }
}