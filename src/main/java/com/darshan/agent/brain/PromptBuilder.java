 package com.darshan.agent.brain;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.context.ConversationManager;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import com.darshan.agent.self.SelfModelEngine;
import org.springframework.stereotype.Component;

/**
 * Single source of prompt creation.
 * Builds rich prompts from profile, goals, semantic memory, current lesson, and conversation summary.
 */
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
     * Build a comprehensive prompt with all available context.
     * The user's current message MUST dominate the prompt.
     * @param isLearningIntent If true, include lesson context; otherwise exclude it.
     */
    public String buildFullPrompt(String input, String instruction, ConversationContext context, boolean isLearningIntent) {
        StringBuilder prompt = new StringBuilder();

        // System identity - concise, focused prompt
        prompt.append("""
                You are Shree, a personal AI tutor and assistant created by Darshan.

                RULES:
                - Answer the user's question directly and accurately
                - Use **bold** for key terms
                - Use markdown formatting for readability
                - Be warm, encouraging, and educational
                - Keep responses focused on the user's question

                """);

        // User profile - only if relevant
        String profileContext = buildProfileContext();
        if (!profileContext.isEmpty()) {
            prompt.append("USER PROFILE:\n").append(profileContext).append("\n");
        }

        // Active goal context - only if relevant to current question
        String goalContext = buildGoalContext();
        if (!goalContext.isEmpty()) {
            prompt.append("ACTIVE GOAL:\n").append(goalContext).append("\n");
        }

        // Current lesson context - ONLY if learning intent is detected
        if (isLearningIntent) {
            String lessonContext = buildLessonContext();
            if (!lessonContext.isEmpty()) {
                prompt.append("CURRENT LESSON:\n").append(lessonContext).append("\n");
            }
        }

        // Memory from past interactions - limit to most relevant
        String memory = context.getWorkingMemory();
        if (memory != null && !memory.isEmpty()) {
            // Truncate memory to prevent prompt bloat
            String truncatedMemory = memory.length() > 500 ? memory.substring(0, 500) + "..." : memory;
            prompt.append("PAST EXPERIENCES:\n").append(truncatedMemory).append("\n");
        }

        // Conversation history - limit to last 3 exchanges
        String history = context.getConversationSummary();
        if (history != null && !history.isEmpty()) {
            // Only include last 3 lines of conversation history
            String[] lines = history.split("\n");
            int start = Math.max(0, lines.length - 6);
            StringBuilder recentHistory = new StringBuilder();
            for (int i = start; i < lines.length; i++) {
                recentHistory.append(lines[i]).append("\n");
            }
            prompt.append("RECENT CONVERSATION:\n").append(recentHistory).append("\n");
        }

        // Executive instruction
        if (instruction != null && !instruction.isEmpty()) {
            prompt.append("INSTRUCTION: ").append(instruction).append("\n");
        }

        // Current input - MUST be last and prominent
        prompt.append("USER: ").append(input).append("\n");

        return prompt.toString();
    }

    /**
     * Overloaded method for backward compatibility (no learning intent = exclude lesson context).
     */
    public String buildFullPrompt(String input, String instruction, ConversationContext context) {
        return buildFullPrompt(input, instruction, context, false);
    }

    /**
     * Build profile context string.
     */
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

    /**
     * Build goal context string.
     */
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

    /**
     * Build lesson context string.
     */
    public String buildLessonContext() {
        if (!conversationManager.hasActiveLesson()) return "";
        return conversationManager.buildProgressSummary();
    }
}