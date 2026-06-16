package com.darshan.agent.brain;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.context.ConversationManager;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.LessonState;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import com.darshan.agent.self.SelfModelEngine;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PromptBuilder {

    private final SelfModelEngine selfModel;
    private final MemoryFacade memoryFacade;
    private final UserProfile userProfile;
    private final GoalManager goalManager;
    private final SemanticMemoryEngine semantic;
    private final com.darshan.agent.context.ConversationManager conversationManager;

    public PromptBuilder(
            SelfModelEngine selfModel,
            MemoryFacade memoryFacade,
            UserProfile userProfile,
            GoalManager goalManager,
            SemanticMemoryEngine semantic,
            @org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager") com.darshan.agent.context.ConversationManager conversationManager) {
        this.selfModel = selfModel;
        this.memoryFacade = memoryFacade;
        this.userProfile = userProfile;
        this.goalManager = goalManager;
        this.semantic = semantic;
        this.conversationManager = conversationManager;
    }

    /**
     * Build a comprehensive prompt with all available context including knowledge graph facts.
     */
    public String buildFullPrompt(String input, String instruction, ConversationContext context, boolean isLearningIntent, List<String> graphFacts) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are Shree, a personal AI tutor and assistant created by Darshan.

                RULES:
                - Answer the user's question directly and accurately
                - Use **bold** for key terms
                - Use markdown formatting for readability
                - Be warm, encouraging, and educational
                - Keep responses focused on the user's question
                - NEVER fabricate memory claims. Only reference information explicitly provided in the PAST EXPERIENCES or KNOWN FACTS sections below.
                - If no PAST EXPERIENCES section is provided, do NOT say things like "as you mentioned before", "from our previous discussion", or "you already learned".
                - NEVER claim to remember things that are not in the provided context.
                - For roadmaps, learning plans, or structured content: provide specific topic names, project ideas, and concrete resources. Never use placeholder text like "[Insert links here]", "[Add resource]", or generic templates.
                - Every roadmap must include: specific level names, concrete topics, real project ideas, named resources, and measurable milestones.

                """);

        String profileContext = buildProfileContext();
        if (!profileContext.isEmpty()) {
            prompt.append("USER PROFILE:\n").append(profileContext).append("\n");
        }

        String goalContext = buildGoalContext();
        if (!goalContext.isEmpty()) {
            prompt.append("ACTIVE GOAL:\n").append(goalContext).append("\n");
        }

        if (isLearningIntent) {
            String lessonContext = buildLessonContext();
            if (!lessonContext.isEmpty()) {
                prompt.append("CURRENT LESSON:\n").append(lessonContext).append("\n");
            }
        }

        String memory = context.getWorkingMemory();
        if (memory != null && !memory.isEmpty()) {
            String truncatedMemory = memory.length() > 500 ? memory.substring(0, 500) + "..." : memory;
            prompt.append("PAST EXPERIENCES:\n").append(truncatedMemory).append("\n");
        }

        if (graphFacts != null && !graphFacts.isEmpty()) {
            prompt.append("KNOWN FACTS:\n");
            for (String fact : graphFacts) {
                prompt.append("- ").append(fact).append("\n");
            }
            prompt.append("\n");
        }

        String history = context.getConversationSummary();
        if (history != null && !history.isEmpty()) {
            String[] lines = history.split("\n");
            int start = Math.max(0, lines.length - 6);
            StringBuilder recentHistory = new StringBuilder();
            for (int i = start; i < lines.length; i++) {
                recentHistory.append(lines[i]).append("\n");
            }
            prompt.append("RECENT CONVERSATION:\n").append(recentHistory).append("\n");
        }

        if (instruction != null && !instruction.isEmpty()) {
            prompt.append("INSTRUCTION: ").append(instruction).append("\n");
        }

        prompt.append("USER: ").append(input).append("\n");

        return prompt.toString();
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context, boolean isLearningIntent) {
        return buildFullPrompt(input, instruction, context, isLearningIntent, null);
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context) {
        return buildFullPrompt(input, instruction, context, false, null);
    }

    public String buildProfileContext() {
        StringBuilder sb = new StringBuilder();
        if (userProfile.getName() != null && !userProfile.getName().isBlank()) {
            sb.append("Name: ").append(userProfile.getName()).append("\n");
        }
        if (userProfile.getTeachingStyle() != null) {
            sb.append("Preferred teaching style: ").append(userProfile.getTeachingStyle()).append("\n");
        }
        if (!userProfile.getPreferences().isEmpty()) {
            sb.append("Interests: ");
            userProfile.getPreferences().forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
            sb.append("\n");
        }
        return sb.toString();
    }

    public String buildGoalContext() {
        if (!goalManager.hasGoal()) return "";
        AgentGoal goal = goalManager.getGoal();
        if (goal == null) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Goal: ").append(goal.getDescription()).append("\n");
        if (!goal.getSubGoals().isEmpty()) {
            long completed = goal.getSubGoals().stream().filter(g -> g.isCompleted()).count();
            sb.append("Progress: ").append(completed).append("/").append(goal.getSubGoals().size()).append(" steps completed\n");
            goal.getSubGoals().stream()
                    .filter(g -> !g.isCompleted())
                    .findFirst()
                    .ifPresent(g -> sb.append("Current step: ").append(g.getDescription()).append("\n"));
        }
        return sb.toString();
    }

    public String buildLessonContext() {
        return "";
    }

    public String buildLessonContext(LessonState lessonState) {
        if (lessonState == null || !lessonState.hasActiveLesson()) return "";
        return lessonState.buildProgressSummary();
    }
}