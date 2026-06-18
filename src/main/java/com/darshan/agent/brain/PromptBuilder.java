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

    public String buildFullPrompt(String input, String instruction, ConversationContext context,
                                   boolean isLearningIntent, List<String> graphFacts,
                                   List<String> projectFacts, List<String> chiefInsights,
                                   List<String> planFacts) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("""
                You are Shree, a personal AI tutor and assistant created by Darshan.

                RULES:
                - Answer the user's question directly and accurately
                - Use **bold** for key terms
                - Use markdown formatting for readability
                - Be warm, encouraging, and educational
                - Keep responses focused on the user's question
                - NEVER fabricate memory claims. Only reference information explicitly provided below.
                - For roadmaps, learning plans, or structured content: provide specific topic names, project ideas, and concrete resources.

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

        if (projectFacts != null && !projectFacts.isEmpty()) {
            prompt.append("PROJECT STATUS:\n");
            for (String fact : projectFacts) {
                prompt.append("- ").append(fact).append("\n");
            }
            prompt.append("\n");
        }

        if (chiefInsights != null && !chiefInsights.isEmpty()) {
            prompt.append("CHIEF OF STAFF INSIGHT:\n");
            for (String insight : chiefInsights) {
                prompt.append("- ").append(insight).append("\n");
            }
            prompt.append("\n");
        }

        if (planFacts != null && !planFacts.isEmpty()) {
            prompt.append("ACTIVE PLAN:\n");
            for (String fact : planFacts) {
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

    // Backward compatible overloads
    public String buildFullPrompt(String input, String instruction, ConversationContext context,
                                   boolean isLearningIntent, List<String> graphFacts, List<String> projectFacts) {
        return buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, projectFacts, null, null);
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context,
                                   boolean isLearningIntent, List<String> graphFacts, List<String> projectFacts,
                                   List<String> chiefInsights) {
        return buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, projectFacts, chiefInsights, null);
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context,
                                   boolean isLearningIntent, List<String> graphFacts) {
        return buildFullPrompt(input, instruction, context, isLearningIntent, graphFacts, null, null);
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context,
                                   boolean isLearningIntent) {
        return buildFullPrompt(input, instruction, context, isLearningIntent, null, null, null);
    }

    public String buildFullPrompt(String input, String instruction, ConversationContext context) {
        return buildFullPrompt(input, instruction, context, false, null, null, null);
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