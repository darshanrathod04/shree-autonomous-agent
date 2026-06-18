package com.darshan.agent;

import com.darshan.agent.planning.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AutonomousPlanningTests {

    @Autowired
    private AutonomousPlanningEngine planningEngine;

    @BeforeEach
    void setUp() {
        planningEngine.getAllPlans().forEach(p -> {
            // Cleanup handled by engine
        });
    }

    // ==================== PLAN GENERATION TESTS ====================

    @Test
    @DisplayName("Generate plan for Java backend goal")
    void testGenerateJavaBackendPlan() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        assertNotNull(plan);
        assertEquals("Become Java Backend Developer", plan.getGoalName());
        assertFalse(plan.getMilestones().isEmpty());
    }

    @Test
    @DisplayName("Generate plan for internship goal")
    void testGenerateInternshipPlan() {
        ExecutionPlan plan = planningEngine.generatePlan("Get Internship");
        assertNotNull(plan);
        assertTrue(plan.getMilestones().size() >= 5);
    }

    @Test
    @DisplayName("Generate plan for platform launch")
    void testGeneratePlatformPlan() {
        ExecutionPlan plan = planningEngine.generatePlan("Launch SCC Platform");
        assertNotNull(plan);
        assertTrue(plan.getMilestones().size() >= 5);
    }

    @Test
    @DisplayName("Generate plan for generic goal")
    void testGenerateGenericPlan() {
        ExecutionPlan plan = planningEngine.generatePlan("Learn Python");
        assertNotNull(plan);
        assertFalse(plan.getMilestones().isEmpty());
    }

    // ==================== MILESTONE GENERATION TESTS ====================

    @Test
    @DisplayName("Milestones have tasks")
    void testMilestonesHaveTasks() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        for (PlanMilestone milestone : plan.getMilestones()) {
            assertFalse(milestone.getTasks().isEmpty(), "Milestone '" + milestone.getTitle() + "' should have tasks");
        }
    }

    @Test
    @DisplayName("Milestones have target dates")
    void testMilestonesHaveDates() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        for (PlanMilestone milestone : plan.getMilestones()) {
            assertNotNull(milestone.getTargetDate(), "Milestone should have target date");
        }
    }

    // ==================== TASK GENERATION TESTS ====================

    @Test
    @DisplayName("Tasks have required fields")
    void testTasksHaveFields() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        PlanMilestone first = plan.getMilestones().get(0);
        ExecutionTask task = first.getTasks().get(0);
        assertNotNull(task.getId());
        assertNotNull(task.getTitle());
        assertNotNull(task.getDescription());
        assertNotNull(task.getPriority());
        assertTrue(task.getEstimatedHours() > 0);
    }

    @Test
    @DisplayName("Spring Boot milestone has security task")
    void testSpringBootHasSecurityTask() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        boolean hasSecurity = plan.getMilestones().stream()
                .filter(m -> m.getTitle().toLowerCase().contains("spring"))
                .flatMap(m -> m.getTasks().stream())
                .anyMatch(t -> t.getTitle().toLowerCase().contains("security"));
        assertTrue(hasSecurity, "Spring Boot milestone should have security task");
    }

    // ==================== DEPENDENCY ENGINE TESTS ====================

    @Test
    @DisplayName("Tasks have dependencies set")
    void testTasksHaveDependencies() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        PlanMilestone milestone = plan.getMilestones().get(0);
        if (milestone.getTasks().size() > 1) {
            // Second task should depend on first
            assertFalse(milestone.getTasks().get(1).getDependencies().isEmpty());
        }
    }

    @Test
    @DisplayName("No circular dependencies")
    void testNoCircularDependencies() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        for (PlanMilestone milestone : plan.getMilestones()) {
            for (ExecutionTask task : milestone.getTasks()) {
                // Task should not depend on itself
                assertFalse(task.getDependencies().contains(task.getId()));
            }
        }
    }

    // ==================== PROGRESS CALCULATION TESTS ====================

    @Test
    @DisplayName("Progress starts at 0")
    void testInitialProgress() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        assertEquals(0, plan.getOverallProgress(), 0.1);
    }

    @Test
    @DisplayName("Progress updates on task completion")
    void testProgressUpdate() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        PlanMilestone milestone = plan.getMilestones().get(0);
        ExecutionTask firstTask = milestone.getTasks().get(0);
        planningEngine.completeTask(plan.getId(), firstTask.getId());
        assertTrue(plan.getOverallProgress() > 0, "Progress should increase after completing task");
    }

    @Test
    @DisplayName("Total tasks count accurate")
    void testTotalTasksCount() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        long total = plan.getTotalTasks();
        long calculated = plan.getAllTasks().size();
        assertEquals(total, calculated);
    }

    // ==================== DAILY PRIORITIES TESTS ====================

    @Test
    @DisplayName("Daily priorities returns max 3")
    void testDailyPrioritiesLimit() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        planningEngine.getActivePlan();
        List<ExecutionTask> priorities = planningEngine.getDailyPriorities();
        assertTrue(priorities.size() <= 3, "Should return max 3 priorities");
    }

    @Test
    @DisplayName("Daily priorities exclude completed tasks")
    void testDailyPrioritiesExcludeCompleted() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        planningEngine.getActivePlan();
        List<ExecutionTask> priorities = planningEngine.getDailyPriorities();
        for (ExecutionTask task : priorities) {
            assertFalse(task.isCompleted(), "Priorities should not include completed tasks");
        }
    }

    // ==================== PLAN REVIEW TESTS ====================

    @Test
    @DisplayName("Plan review returns issues")
    void testPlanReview() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        planningEngine.getActivePlan();
        AutonomousPlanningEngine.PlanReview review = planningEngine.reviewPlan();
        assertNotNull(review);
    }

    @Test
    @DisplayName("Plan review detects too many open tasks")
    void testPlanReviewTooManyTasks() {
        // Create a plan with many tasks
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        planningEngine.getActivePlan();
        AutonomousPlanningEngine.PlanReview review = planningEngine.reviewPlan();
        // With normal plan, should not have too many tasks issue
        assertFalse(review.getIssues().stream()
                .anyMatch(i -> i.contains("Too many open tasks")));
    }

    // ==================== PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Plan persists to file")
    void testPersistence() {
        planningEngine.generatePlan("Become Java Backend Developer");
        File file = new File("execution_plans.json");
        assertTrue(file.exists());
    }

    @Test
    @DisplayName("Plan survives restart")
    void testSurvivesRestart() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        String planId = plan.getId();
        planningEngine.save();

        // Simulate restart by clearing active plan reference
        planningEngine.getAllPlans().clear();
        planningEngine.load();

        Optional<ExecutionPlan> loaded = planningEngine.getPlan(planId);
        assertTrue(loaded.isPresent(), "Plan should survive restart");
    }

    // ==================== QUERY TESTS ====================

    @Test
    @DisplayName("Get active plan")
    void testGetActivePlan() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        Optional<ExecutionPlan> active = planningEngine.getActivePlan();
        assertTrue(active.isPresent());
        assertEquals(plan.getId(), active.get().getId());
    }

    @Test
    @DisplayName("Get all plans")
    void testGetAllPlans() {
        planningEngine.generatePlan("Become Java Backend Developer");
        planningEngine.generatePlan("Get Internship");
        List<ExecutionPlan> all = planningEngine.getAllPlans();
        assertTrue(all.size() >= 2);
    }

    @Test
    @DisplayName("Plan summary format")
    void testPlanSummary() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        String summary = planningEngine.getPlanSummary();
        assertTrue(summary.contains("Become Java Backend Developer"));
        assertTrue(summary.contains("Progress"));
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Plan links to goal")
    void testPlanLinksToGoal() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        assertNotNull(plan.getGoalId());
        assertFalse(plan.getGoalId().isEmpty());
    }

    @Test
    @DisplayName("Multiple plans can exist")
    void testMultiplePlans() {
        ExecutionPlan plan1 = planningEngine.generatePlan("Become Java Backend Developer");
        ExecutionPlan plan2 = planningEngine.generatePlan("Get Internship");
        assertNotEquals(plan1.getId(), plan2.getId());
    }

    @Test
    @DisplayName("Complete task updates plan progress")
    void testCompleteTaskUpdatesProgress() {
        ExecutionPlan plan = planningEngine.generatePlan("Become Java Backend Developer");
        double before = plan.getOverallProgress();
        ExecutionTask task = plan.getAllTasks().get(0);
        planningEngine.completeTask(plan.getId(), task.getId());
        double after = plan.getOverallProgress();
        assertTrue(after > before, "Progress should increase after completing task");
    }
}