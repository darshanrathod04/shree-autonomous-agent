package com.darshan.agent.brain;

import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.cognition.*;
import com.darshan.agent.planning.AutonomousPlanningEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.LessonEngine;
import com.darshan.agent.context.LessonState;
import com.darshan.agent.debate.swarm.DebateSwarmEngine;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentBrain {

    private final CognitiveGovernorEngine governor;
    private final ConversationStateMachine stateMachine;
    private final IntentEngine intentEngine;
    private final SkillRouter router;
    private final DebateSwarmEngine swarm;
    private final PersonalityEngine personality;
    private final MemoryFacade memoryFacade;
    private final MetaCognitionEngine meta;
    private final IdentityPerceptionEngine identityPerceptionEngine;
    private final LessonEngine lessonEngine;
    private final PromptBuilder promptBuilder;
    private final OllamaClient ollamaClient;
    private final KnowledgeGraphEngine knowledgeGraph;
    private final ProjectIntelligenceEngine projectIntelligence;
    private final ChiefOfStaffEngine chiefOfStaff;
    private final AutonomousPlanningEngine planningEngine;

    public AgentBrain(
            CognitiveGovernorEngine governor,
            ConversationStateMachine stateMachine,
            IntentEngine intentEngine,
            SkillRouter router,
            DebateSwarmEngine swarm,
            PersonalityEngine personality,
            MemoryFacade memoryFacade,
            MetaCognitionEngine meta,
            IdentityPerceptionEngine identityPerceptionEngine,
            LessonEngine lessonEngine,
            PromptBuilder promptBuilder,
            OllamaClient ollamaClient,
            KnowledgeGraphEngine knowledgeGraph,
            ProjectIntelligenceEngine projectIntelligence,
            ChiefOfStaffEngine chiefOfStaff,
            AutonomousPlanningEngine planningEngine
    ) {
        this.governor = governor;
        this.stateMachine = stateMachine;
        this.intentEngine = intentEngine;
        this.router = router;
        this.swarm = swarm;
        this.personality = personality;
        this.memoryFacade = memoryFacade;
        this.meta = meta;
        this.identityPerceptionEngine = identityPerceptionEngine;
        this.lessonEngine = lessonEngine;
        this.promptBuilder = promptBuilder;
        this.ollamaClient = ollamaClient;
        this.knowledgeGraph = knowledgeGraph;
        this.projectIntelligence = projectIntelligence;
        this.chiefOfStaff = chiefOfStaff;
        this.planningEngine = planningEngine;
    }

    // =====================================================
    // MAIN COGNITIVE PIPELINE
    // =====================================================
    public AgentResponse process(
            String input,
            ConversationContext context,
            LessonState lessonState
    ) throws Exception {

        if (context == null) {
            context = new ConversationContext();
        }

        // 1. GOVERNOR (Safety + Stability)
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

        // 2. STATE MACHINE
        String stateReply = stateMachine.handle(input, context);
        if (stateReply != null) {
            return new AgentResponse(stateReply, false);
        }

        // 3. IDENTITY PERCEPTION (extract user name + interests)
        identityPerceptionEngine.perceive(input);

        // 3b. KNOWLEDGE GRAPH EXTRACTION
        knowledgeGraph.extractFromInput(input);

        // 3c. PROJECT INTELLIGENCE EXTRACTION
        projectIntelligence.extractFromInput(input);

        // 4. MEMORY RECALL
        String recalledMemory = memoryFacade.recallAll(input);
        context.setWorkingMemory(recalledMemory);

        // 5. INTENT DETECTION
        String intent = intentEngine.detectIntent(input);
        context.setLastIntent(intent);

        // 6. HANDLE LESSON NAVIGATION INTENTS
        switch (intent) {
            case "LEARN": {
                String topic = input.replaceFirst("(?i)learn\\s+", "").trim();
                if (topic.isEmpty()) topic = "general topics";
                String result = lessonEngine.startLesson(topic, lessonState);
                return new AgentResponse(result, false);
            }
            case "CONTINUE": {
                String result = lessonEngine.nextChapter(lessonState);
                return new AgentResponse(result, false);
            }
            case "PREVIOUS": {
                String result = lessonEngine.previousChapter(lessonState);
                return new AgentResponse(result, false);
            }
            case "SUMMARY": {
                String result = lessonEngine.getSummary(lessonState);
                return new AgentResponse(result, false);
            }
            case "QUIZ": {
                String result = lessonEngine.quizMode(lessonState);
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

        // 7. SKILL ROUTING
        Skill skill = router.route(intent);
        String rawReply;

        if (skill != null) {
            rawReply = skill.execute(input, context);
        } else {
            boolean isLearningIntent = isLearningIntent(intent);
            String instruction = buildInstruction(intent, isLearningIntent, lessonState);
            List<String> graphFacts = knowledgeGraph.getContextFacts(input);
            List<String> projectFacts = projectIntelligence.getContextFacts(input);
            List<String> chiefInsights = chiefOfStaff.getContextInsights();
            List<String> planFacts = planningEngine.getActivePlan()
                    .map(p -> List.of(planningEngine.getPlanSummary()))
                    .orElse(List.of());
            String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, projectFacts, chiefInsights, planFacts);
            rawReply = ollamaClient.generateDirect(fullPrompt);
        }

        // 8. META COGNITION
        MetaThought reflection = meta.evaluate(input, rawReply);
        if (!reflection.isSuccessful()) {
            rawReply += "\n\n(Self-correction applied)";
        }

        // 9. POST-PROCESSING: Strip placeholders and fake content
        rawReply = stripPlaceholders(rawReply);

        // 10. PERSONALITY RENDER
        String finalReply = personality.applyPersonality(rawReply);

        return new AgentResponse(finalReply, false);
    }

    public AgentResponse process(
            String input,
            ConversationContext context
    ) throws Exception {
        return process(input, context, new LessonState());
    }

    private boolean isLearningIntent(String intent) {
        return "LEARN".equals(intent) || "CONTINUE".equals(intent)
                || "PREVIOUS".equals(intent) || "SUMMARY".equals(intent)
                || "QUIZ".equals(intent);
    }

    private String buildInstruction(String intent, boolean isLearningIntent, LessonState lessonState) {
        if (isLearningIntent && lessonState.hasActiveLesson()) {
            return "Teach the user about " + lessonState.getActiveTopic()
                    + ", chapter " + lessonState.getChapterNumber()
                    + ". Use a teaching tone.";
        }
        return "Respond naturally and helpfully.";
    }

    private String stripPlaceholders(String reply) {
        if (reply == null) return reply;
        reply = reply.replaceAll("(?i)\\[insert[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[add[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[your[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[link[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[placeholder[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[TODO[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[TBD[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)\\[fill[^\\]]*\\]", "");
        reply = reply.replaceAll("(?i)Click here to learn more\\.", "");
        reply = reply.replaceAll("(?i)Learn more at \\[.*?\\]\\.", "");
        reply = reply.replaceAll("(?i)\\(insert link\\)", "");
        reply = reply.replaceAll("\\n{3,}", "\n\n");
        return reply.trim();
    }
}