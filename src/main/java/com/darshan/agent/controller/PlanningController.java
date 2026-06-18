 package com.darshan.agent.controller;

import com.darshan.agent.planning.*;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent/plans")
public class PlanningController {

    private final AutonomousPlanningEngine planningEngine;

    public PlanningController(AutonomousPlanningEngine planningEngine) {
        this.planningEngine = planningEngine;
    }

    @GetMapping
    public Map<String, Object> getAllPlans() {
        Map<String, Object> result = new HashMap<>();
        result.put("plans", planningEngine.getAllPlans());
        result.put("count", planningEngine.getAllPlans().size());
        return result;
    }

    @GetMapping("/active")
    public Map<String, Object> getActivePlan() {
        Map<String, Object> result = new HashMap<>();
        planningEngine.getActivePlan().ifPresent(plan -> {
            result.put("id", plan.getId());
            result.put("goalName", plan.getGoalName());
            result.put("status", plan.getStatus().name());
            result.put("progress", plan.getOverallProgress());
            result.put("milestones", plan.getMilestones());
        });
        return result;
    }

    @GetMapping("/priorities")
    public Map<String, Object> getDailyPriorities() {
        Map<String, Object> result = new HashMap<>();
        List<ExecutionTask> priorities = planningEngine.getDailyPriorities();
        result.put("priorities", priorities);
        result.put("count", priorities.size());
        return result;
    }

    @GetMapping("/review")
    public Map<String, Object> reviewPlan() {
        Map<String, Object> result = new HashMap<>();
        AutonomousPlanningEngine.PlanReview review = planningEngine.reviewPlan();
        result.put("hasIssues", review.hasIssues());
        result.put("issues", review.getIssues());
        result.put("recommendations", review.getRecommendations());
        return result;
    }

    @GetMapping("/progress")
    public Map<String, Object> getProgress() {
        Map<String, Object> result = new HashMap<>();
        planningEngine.getActivePlan().ifPresent(plan -> {
            result.put("overallProgress", plan.getOverallProgress());
            result.put("totalTasks", plan.getTotalTasks());
            result.put("completedTasks", plan.getCompletedTasks());
            result.put("milestoneProgress", plan.getMilestones().stream()
                    .collect(Collectors.toMap(
                            PlanMilestone::getTitle,
                            PlanMilestone::getProgress
                    )));
        });
        return result;
    }

    @PostMapping("/generate")
    public Map<String, Object> generatePlan(@RequestBody Map<String, String> request) {
        Map<String, Object> result = new HashMap<>();
        String goal = request.get("goal");
        if (goal == null || goal.isEmpty()) {
            result.put("error", "Goal is required");
            return result;
        }
        ExecutionPlan plan = planningEngine.generatePlan(goal);
        result.put("id", plan.getId());
        result.put("goalName", plan.getGoalName());
        result.put("status", plan.getStatus().name());
        result.put("milestones", plan.getMilestones());
        result.put("totalTasks", plan.getTotalTasks());
        return result;
    }

    @PostMapping("/task/{taskId}/complete")
    public Map<String, Object> completeTask(@PathVariable String taskId) {
        Map<String, Object> result = new HashMap<>();
        planningEngine.getActivePlan().ifPresent(plan -> {
            boolean success = planningEngine.completeTask(plan.getId(), taskId);
            result.put("success", success);
            if (success) {
                result.put("progress", plan.getOverallProgress());
            }
        });
        if (!result.containsKey("success")) {
            result.put("success", false);
        }
        return result;
    }
}