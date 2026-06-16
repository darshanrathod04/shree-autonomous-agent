package com.darshan.agent.controller;

import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import com.darshan.agent.personality.PersonalityEngine;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Read-only dashboard endpoints for the frontend.
 * These expose system state without modifying anything.
 */
@RestController
@RequestMapping("/agent")
public class DashboardController {

    private final GoalManager goalManager;
    private final UserProfile userProfile;
    private final SemanticMemoryEngine semanticMemory;
    private final EpisodicMemoryEngine episodicMemory;
    private final PersonalityEngine personalityEngine;

    public DashboardController(
            GoalManager goalManager,
            UserProfile userProfile,
            SemanticMemoryEngine semanticMemory,
            EpisodicMemoryEngine episodicMemory,
            PersonalityEngine personalityEngine) {
        this.goalManager = goalManager;
        this.userProfile = userProfile;
        this.semanticMemory = semanticMemory;
        this.episodicMemory = episodicMemory;
        this.personalityEngine = personalityEngine;
    }

    @GetMapping("/goals")
    public Map<String, Object> getGoals() {
        Map<String, Object> result = new HashMap<>();
        if (goalManager.hasGoal() && goalManager.getGoal() != null) {
            var goal = goalManager.getGoal();
            long completed = goal.getSubGoals().stream()
                    .filter(g -> g.isCompleted()).count();
            long total = goal.getSubGoals().size();

            result.put("hasGoal", true);
            result.put("description", goal.getDescription());
            result.put("completed", goal.isCompleted());
            result.put("totalSubGoals", total);
            result.put("completedSubGoals", completed);
            result.put("progressPercent", total > 0 ? (completed * 100 / total) : 0);
            result.put("createdAt", goal.getCreatedAt());

            // Subgoals
            var subGoals = goal.getSubGoals().stream().map(sg -> {
                Map<String, Object> sgMap = new HashMap<>();
                sgMap.put("description", sg.getDescription());
                sgMap.put("completed", sg.isCompleted());
                return sgMap;
            }).toList();
            result.put("subGoals", subGoals);
        } else {
            result.put("hasGoal", false);
        }
        return result;
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile() {
        Map<String, Object> result = new HashMap<>();
        result.put("name", userProfile.getName());
        result.put("teachingStyle", userProfile.getTeachingStyle());
        result.put("preferredTone", userProfile.getPreferredTone());
        result.put("preferences", userProfile.getPreferences());
        result.put("personalityMode", personalityEngine.getModeName());
        return result;
    }

    @GetMapping("/memory")
    public Map<String, Object> getMemory() {
        Map<String, Object> result = new HashMap<>();
        result.put("semanticCount", semanticMemory.size());
        result.put("episodicCount", episodicMemory.all().size());
        result.put("activeTopic", "Session-scoped (see per-session API)");
        result.put("lessonName", "Session-scoped (see per-session API)");
        result.put("chapterNumber", "Session-scoped (see per-session API)");
        result.put("currentObjective", "Session-scoped (see per-session API)");
        result.put("completedChapters", "Session-scoped (see per-session API)");
        result.put("hasActiveLesson", false);
        return result;
    }

    @GetMapping("/lesson")
    public Map<String, Object> getLesson() {
        Map<String, Object> result = new HashMap<>();
        result.put("hasActiveLesson", false);
        result.put("lessonName", null);
        result.put("activeTopic", null);
        result.put("chapterNumber", 0);
        result.put("currentObjective", null);
        result.put("completedChapters", java.util.List.of());
        result.put("pendingFollowups", java.util.List.of());
        result.put("progressSummary", "Lesson state is now per-session. Use GET /agent/session/{sessionId} endpoint.");
        return result;
    }
}