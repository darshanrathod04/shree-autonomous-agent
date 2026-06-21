package com.darshan.agent.brain;

import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.cognition.*;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.planning.AutonomousPlanningEngine;
import com.darshan.agent.planning.ExecutionPlan;
import com.darshan.agent.planning.ExecutionTask;
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
import java.util.Optional;

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

        // 3. IDENTITY PERCEPTION (extract user name + interests) with per-session context
        identityPerceptionEngine.perceive(input, context);

        // 3b. KNOWLEDGE GRAPH EXTRACTION
        knowledgeGraph.extractFromInput(input);

        // 3c. PROJECT INTELLIGENCE EXTRACTION
        projectIntelligence.extractFromInput(input);

        // 4. MEMORY RECALL
        String recalledMemory = memoryFacade.recallAll(input);
        context.setWorkingMemory(recalledMemory);

        // ROADMAP SELECTION FLOW

        if ("ROADMAP_SELECTION".equals(context.getPendingAction())) {

            context.setPendingAction(null);

            System.out.println("[ROADMAP] User selected=" + input);

            ExecutionPlan plan =
                    planningEngine.generatePlan(input);

            String planSummary =
                    planningEngine.getPlanSummary();

            return new AgentResponse(
                    "📋 Roadmap Created\n\n"
                            + planSummary,
                    false
            );
        }

        // 5. INTENT DETECTION
        String intent = intentEngine.detectIntent(input);
        context.setLastIntent(intent);
        System.out.println("[AgentBrain] DETECTED INTENT: " + intent);

        // 6. HANDLE INTENTS
        switch (intent) {
            case "ROADMAP_REQUEST": {

                context.setPendingAction(
                        "ROADMAP_SELECTION"
                );

                return new AgentResponse(
                        """
                        Which roadmap would you like?
            
                        1. Java Developer
                        2. Spring Boot
                        3. DSA
                        4. Interview Preparation
                        5. AI Engineer
                        """,
                        false
                );
            }
            case "WHO_AM_I": {
                System.out.println("[AgentBrain] EXECUTING WHO_AM_I BRANCH");
                String name = context.getUserName();
                System.out.println("[AgentBrain] WHO_AM_I: context.getUserName() = " + name);
                if (name != null && !name.isEmpty()) {
                    System.out.println("[AgentBrain] WHO_AM_I: Returning session name: " + name);
                    return new AgentResponse("Your name is " + name + ".", false);
                }
                // No global fallback — each session has isolated identity.
                // Session C starting fresh should NOT see Session A's name.
                System.out.println("[AgentBrain] WHO_AM_I: No name in context, returning default");
                return new AgentResponse("I don't know your name yet. Please tell me your name.", false);
            }
            case "PLAN": {
                System.out.println("[AgentBrain] EXECUTING PLAN BRANCH");
                String planDescription = input.replaceFirst("(?i)(i want to become a|become a|plan|roadmap|career path|learning path|steps to|how do i become|how to become)\\s*", "").trim();
                if (planDescription.isEmpty()) planDescription = input;
                ExecutionPlan plan = planningEngine.generatePlan(planDescription);
                String planSummary = planningEngine.getPlanSummary();
                String responseText = "📋 **Roadmap Created**\n\n" + planSummary + "\n\nI've broken this down into milestones and tasks. Check the Planning tab for full details, or ask me about your daily priorities!";
                System.out.println("[AgentBrain] PLAN: Returning roadmap directly (no LLM)");
                return new AgentResponse(responseText, false);
            }
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
            case "NEXT_STEP": {
                System.out.println("[NEXT_STEP] Intent detected");
                
                Optional<ExecutionPlan> activePlanOpt = planningEngine.getActivePlan();
                if (activePlanOpt.isEmpty()) {
                    System.out.println("[NEXT_STEP] No active plan found");
                    return new AgentResponse("No active roadmap. Say 'plan: <your goal>' to create one.", false);
                }
                
                ExecutionPlan plan = activePlanOpt.get();
                System.out.println("[NEXT_STEP] goal=" + plan.getGoalName());
                
                List<ExecutionTask> allTasks = plan.getAllTasks();
                Optional<ExecutionTask> nextTaskOpt = allTasks.stream()
                        .filter(t -> !t.isCompleted() && !t.isBlocked())
                        .findFirst();
                
                if (nextTaskOpt.isEmpty()) {
                    System.out.println("[NEXT_STEP] No pending tasks found");
                    return new AgentResponse("All tasks completed! 🎉 Your roadmap is done.", false);
                }
                
                ExecutionTask nextTask = nextTaskOpt.get();
                System.out.println("[NEXT_STEP] task=" + nextTask.getTitle());
                
                StringBuilder response = new StringBuilder();
                response.append("📋 **Next Task**\n\n");
                response.append("**").append(nextTask.getTitle()).append("**\n");
                response.append(nextTask.getDescription()).append("\n\n");
                response.append("⏱️ Estimated: ").append((int) nextTask.getEstimatedHours()).append(" hours\n");
                response.append("📊 Priority: ").append(nextTask.getPriority()).append("\n");
                response.append("📈 Progress: ").append(String.format("%.0f%%", plan.getOverallProgress()));
                
                System.out.println("[NEXT_STEP] returned without LLM");
                return new AgentResponse(response.toString(), false);
            }

        }

        // 7. SKILL ROUTING
        Skill skill = router.route(intent);
        System.out.println("[AgentBrain] SKILL ROUTING | intent=" + intent + " | skill=" + (skill != null ? skill.getClass().getSimpleName() : "null"));
        String rawReply;

        if (skill != null) {
            System.out.println("[AgentBrain] EXECUTING SKILL: " + skill.getClass().getSimpleName());
            rawReply = skill.execute(input, context);
        } else {
            System.out.println("[AgentBrain] FALLING THROUGH TO LLM | intent=" + intent);
            boolean isLearningIntent = isLearningIntent(intent);
            String instruction = buildInstruction(intent, isLearningIntent, lessonState);
            List<String> graphFacts = knowledgeGraph.getContextFacts(input);
            List<String> projectFacts = projectIntelligence.getContextFacts(input);
            List<String> chiefInsights = chiefOfStaff.getContextInsights();
            List<String> planFacts = planningEngine.getActivePlan()
                    .map(p -> List.of(planningEngine.getPlanSummary()))
                    .orElse(List.of());
            String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, projectFacts, chiefInsights, planFacts);
            System.out.println("========== TRACE ==========");
            System.out.println("SESSION USER = " + context.getUserName());

            System.out.println("===========================");
            System.out.println("[AgentBrain] LLM PROMPT (first 500 chars): " + fullPrompt.substring(0, Math.min(500, fullPrompt.length())));
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