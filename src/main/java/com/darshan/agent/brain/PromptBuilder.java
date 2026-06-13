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
     */
    public String buildFullPrompt(String input, String instruction, ConversationContext context) {
        StringBuilder prompt = new StringBuilder();

        // System identity
        prompt.append("You are Shree, a personal AI tutor and assistant.\n\n");

        // User profile
        String profileContext = buildProfileContext();
        if (!profileContext.isEmpty()) {
            prompt.append("USER PROFILE:\n").append(profileContext).append("\n");
        }

        // Active goal context
        String goalContext = buildGoalContext();
        if (!goalContext.isEmpty()) {
            prompt.append("ACTIVE GOAL:\n").append(goalContext).append("\n");
        }

        // Current lesson context
        String lessonContext = buildLessonContext();
        if (!lessonContext.isEmpty()) {
            prompt.append("CURRENT LESSON:\n").append(lessonContext).append("\n");
        }

        // Memory from past interactions
        String memory = context.getWorkingMemory();
        if (memory != null && !memory.isEmpty()) {
            prompt.append("PAST EXPERIENCES:\n").append(memory).append("\n");
        }

        // Semantic knowledge
        String semanticHint = semantic.recall(input);
        if (!semanticHint.isEmpty()) {
            prompt.append("RELEVANT KNOWLEDGE:\n").append(semanticHint).append("\n");
        }

        // Conversation history
        String history = context.getConversationSummary();
        if (history != null && !history.isEmpty()) {
            prompt.append("RECENT CONVERSATION:\n").append(history).append("\n");
        }

        // Executive instruction
        if (instruction != null && !instruction.isEmpty()) {
            prompt.append("INSTRUCTION: ").append(instruction).append("\n");
        }

        // Current input
        prompt.append("USER: ").append(input).append("\n");

        return prompt.toString();
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