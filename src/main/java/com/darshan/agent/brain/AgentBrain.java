package com.darshan.agent.brain;

import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.cognition.*;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ContextStore;
import com.darshan.agent.context.LessonEngine;
import com.darshan.agent.debate.swarm.DebateSwarmEngine;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.personality.PersonalityEngine;
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
    private final MemoryFacade memoryFacade;
    private final MetaCognitionEngine meta;
    private final IdentityPerceptionEngine identityPerceptionEngine;
    private final com.darshan.agent.context.ConversationManager conversationManager;
    private final LessonEngine lessonEngine;
    private final PromptBuilder promptBuilder;
    private final OllamaClient ollamaClient;

    public AgentBrain(
            CognitiveGovernorEngine governor,
            ConversationStateMachine stateMachine,
            ContextStore contextStore,
            IntentEngine intentEngine,
            SkillRouter router,
            DebateSwarmEngine swarm,
            PersonalityEngine personality,
            MemoryFacade memoryFacade,
            MetaCognitionEngine meta,
            IdentityPerceptionEngine identityPerceptionEngine,
            @org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager") com.darshan.agent.context.ConversationManager conversationManager,
            LessonEngine lessonEngine,
            PromptBuilder promptBuilder,
            OllamaClient ollamaClient
    ) {
        this.governor = governor;
        this.stateMachine = stateMachine;
        this.contextStore = contextStore;
        this.intentEngine = intentEngine;
        this.router = router;
        this.swarm = swarm;
        this.personality = personality;
        this.memoryFacade = memoryFacade;
        this.meta = meta;
        this.identityPerceptionEngine = identityPerceptionEngine;
        this.conversationManager = conversationManager;
        this.lessonEngine = lessonEngine;
        this.promptBuilder = promptBuilder;
        this.ollamaClient = ollamaClient;
    }

    // =====================================================
    // 🧠 MAIN COGNITIVE PIPELINE
    // =====================================================
    public AgentResponse process(
            String input,
            ConversationContext context
    ) throws Exception {

        if (context == null) {
            context = new ConversationContext();
        }

        // -------------------------------------------------
        // 1️⃣ GOVERNOR (Safety + Stability)
        // -------------------------------------------------
        CognitiveDecision decision =
                governor.evaluate(input, context);

        switch (decision.getAction()) {
            case PAUSE_AGENT:
                return new AgentResponse("I need a short recovery pause.", false);
            case REFUSE:
                return new AgentResponse("I cannot help with that request.", false);
            case ASK_CLARIFICATION:
                return new AgentResponse("Can you clarify what you mean?", false);
        }

        // -------------------------------------------------
        // 2️⃣ STATE MACHINE
        // -------------------------------------------------
        String stateReply = stateMachine.handle(input, context);
        if (stateReply != null) {
            return new AgentResponse(stateReply, false);
        }

        // -------------------------------------------------
        // 3️⃣ IDENTITY PERCEPTION (extract user name + interests)
        // -------------------------------------------------
        identityPerceptionEngine.perceive(input);

        // -------------------------------------------------
        // 4️⃣ MEMORY RECALL
        // -------------------------------------------------
        String recalledMemory = memoryFacade.recallAll(input);
        context.setWorkingMemory(recalledMemory);

        // -------------------------------------------------
        // 5️⃣ INTENT DETECTION
        // -------------------------------------------------
        String intent = intentEngine.detectIntent(input);
        context.setLastIntent(intent);

        // -------------------------------------------------
        // 6️⃣ HANDLE LESSON NAVIGATION INTENTS
        // -------------------------------------------------
        switch (intent) {
            case "LEARN": {
                String topic = input.replaceFirst("(?i)learn\\s+", "").trim();
                if (topic.isEmpty()) topic = "general topics";
                conversationManager.setActiveTopic(topic);
                String result = lessonEngine.startLesson(topic);
                // Session Manager handles all history tracking - no context history manipulation here
                return new AgentResponse(result, false);
            }
            case "CONTINUE": {
                String result = lessonEngine.nextChapter();
                return new AgentResponse(result, false);
            }
            case "PREVIOUS": {
                String result = lessonEngine.previousChapter();
                return new AgentResponse(result, false);
            }
            case "SUMMARY": {
                String result = lessonEngine.getSummary();
                return new AgentResponse(result, false);
            }
            case "QUIZ": {
                String result = lessonEngine.quizMode();
                return new AgentResponse(result, false);
            }
            case "GOAL_QUERY": {
                String goalStatus = promptBuilder.buildGoalContext();
                if (goalStatus.isEmpty()) {
                    goalStatus = "No active goal. Say 'goal: <description>' to set one.";
                }
                return new AgentResponse(goalStatus, false);
            }
        }

        // -------------------------------------------------
        // 7️⃣ SKILL ROUTING
        // -------------------------------------------------
        Skill skill = router.route(intent);
        String rawReply;

        if (skill != null) {
            rawReply = skill.execute(input, context);
        } else {
            // Use PromptBuilder for direct response (bypass swarm for speed)
            boolean isLearningIntent = isLearningIntent(intent);
            String instruction = buildInstruction(intent, isLearningIntent);
            String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context, isLearningIntent);
            rawReply = ollamaClient.generateDirect(fullPrompt);
        }

        // -------------------------------------------------
        // 8️⃣ META COGNITION
        // -------------------------------------------------
        MetaThought reflection = meta.evaluate(input, rawReply);
        if (!reflection.isSuccessful()) {
            rawReply += "\n\n(Self-correction applied)";
        }

        // -------------------------------------------------
        // 9️⃣ PERSONALITY RENDER
        // -------------------------------------------------
        String finalReply = personality.applyPersonality(rawReply);

        return new AgentResponse(finalReply, false);
    }

    private boolean isLearningIntent(String intent) {
        return "LEARN".equals(intent) || "CONTINUE".equals(intent)
                || "PREVIOUS".equals(intent) || "SUMMARY".equals(intent)
                || "QUIZ".equals(intent);
    }

    private String buildInstruction(String intent, boolean isLearningIntent) {
        if (isLearningIntent && conversationManager.hasActiveLesson()) {
            return "Teach the user about " + conversationManager.getActiveTopic()
                    + ", chapter " + conversationManager.getChapterNumber()
                    + ". Use a teaching tone.";
        }
        return "Respond naturally and helpfully.";
    }
}