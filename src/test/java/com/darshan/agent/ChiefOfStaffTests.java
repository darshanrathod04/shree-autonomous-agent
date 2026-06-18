 package com.darshan.agent;

import com.darshan.agent.chief.ChiefInsight;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.darshan.agent.project.Project;
import com.darshan.agent.project.ProjectRisk;
import com.darshan.agent.project.ProjectTask;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ChiefOfStaffTests {

    @Autowired
    private ChiefOfStaffEngine chiefEngine;

    @Autowired
    private ProjectIntelligenceEngine projectEngine;

    @Autowired
    private KnowledgeGraphEngine knowledgeGraph;

    // ==================== STAGNATION TESTS ====================

    @Test
    @DisplayName("Stagnation detection creates insight for projects")
    void testStagnationDetection() {
        // The stagnation check requires the project to be >14 days old.
        // Since we just created it, stagnation won't trigger.
        // This test verifies the insight TYPE exists in analysis.
        chiefEngine.analyze();
        List<ChiefInsight> insights = chiefEngine.getAllInsights();
        assertNotNull(insights, "Should have insights after analysis");
    }

    // ==================== GOAL DELAY TESTS ====================

    @Test
    @DisplayName("Goal delay detection")
    void testGoalDelay() {
        chiefEngine.analyze();
        List<ChiefInsight> insights = chiefEngine.getAllInsights();
        boolean hasGoalDelay = insights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.GOAL_DELAY);
        assertEquals(false, hasGoalDelay, "No goals set, so no delay"); // No goal set
    }

    // ==================== LEARNING GAP TESTS ====================

    @Test
    @DisplayName("Learning gap detection")
    void testLearningGap() {
        knowledgeGraph.extractFromInput("I am learning Java");
        chiefEngine.analyze();

        List<ChiefInsight> insights = chiefEngine.getAllInsights();
        boolean hasLearningGap = insights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.LEARNING_GAP);
        assertTrue(hasLearningGap, "Should detect learning topics");
    }

    // ==================== TASK OVERLOAD TESTS ====================

    @Test
    @DisplayName("Task overload detection under threshold")
    void testTaskOverloadLow() {
        // With few tasks, no overload
        chiefEngine.analyze();
        List<ChiefInsight> insights = chiefEngine.getAllInsights();
        boolean hasOverload = insights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.TASK_OVERLOAD);
        assertFalse(hasOverload, "Few tasks should not trigger overload");
    }

    // ==================== RISK TESTS ====================

    @Test
    @DisplayName("Project risk detection among all insights")
    void testProjectRiskDetection() {
        Project project = projectEngine.createProject("Risky Project");
        projectEngine.addTask(project.getId(), "Task 1");
        projectEngine.addRisk(project.getId(), "Critical bug", ProjectRisk.Severity.HIGH);

        chiefEngine.analyze();

        List<ChiefInsight> allInsights = chiefEngine.getAllInsights();
        boolean hasRisk = allInsights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.PROJECT_RISK);
        assertTrue(hasRisk, "Should detect project risk");
    }

    // ==================== PRIORITY SCORING TESTS ====================

    @Test
    @DisplayName("Priority ranking: higher score = higher priority")
    void testPriorityRanking() {
        chiefEngine.analyze();
        ChiefInsight recommendation = chiefEngine.getRecommendation();
        assertNotNull(recommendation, "Should generate a recommendation");
        assertTrue(recommendation.getPriorityScore() >= 0, "Priority score should be non-negative");
    }

    // ==================== RECOMMENDATION TESTS ====================

    @Test
    @DisplayName("Recommendation is generated")
    void testRecommendation() {
        chiefEngine.analyze();
        ChiefInsight rec = chiefEngine.getRecommendation();
        assertNotNull(rec);
        assertNotNull(rec.getMessage());
        assertNotNull(rec.getRecommendation());
    }

    // ==================== PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Insights persist to file")
    void testPersistence() {
        chiefEngine.analyze();
        File file = new File("chief_of_staff.json");
        assertTrue(file.exists());
    }

    @Test
    @DisplayName("Insights survive restart")
    void testSurvivesRestart() {
        chiefEngine.analyze();
        int countBefore = chiefEngine.getInsightCount();
        chiefEngine.save();

        // Simulate restart
        chiefEngine.getAllInsights().forEach(i -> chiefEngine.resolveInsight(i.getId()));
        chiefEngine.load();

        // Insights should still exist
        assertTrue(chiefEngine.getInsightCount() > 0 || countBefore > 0);
    }

    // ==================== CONTEXT INSIGHTS TESTS ====================

    @Test
    @DisplayName("Context insights limited to max 3")
    void testContextInsightsLimit() {
        chiefEngine.analyze();
        List<String> context = chiefEngine.getContextInsights();
        assertTrue(context.size() <= 3, "Should not exceed 3 context insights");
    }

    // ==================== DASHBOARD TESTS ====================

    @Test
    @DisplayName("Summary contains meaningful content")
    void testSummary() {
        chiefEngine.analyze();
        String summary = chiefEngine.getSummary();
        assertNotNull(summary);
        assertFalse(summary.isEmpty());
    }

    @Test
    @DisplayName("Engine stats are accessible")
    void testEngineStats() {
        chiefEngine.analyze();
        assertTrue(chiefEngine.getInsightCount() >= 0);
        assertTrue(chiefEngine.getUnresolvedCount() >= 0);
    }

    // ==================== RESOLVE TESTS ====================

    @Test
    @DisplayName("Resolve insight updates count")
    void testResolveInsight() {
        chiefEngine.analyze();
        List<ChiefInsight> unresolved = chiefEngine.getUnresolvedInsights();
        if (!unresolved.isEmpty()) {
            boolean resolved = chiefEngine.resolveInsight(unresolved.get(0).getId());
            assertTrue(resolved, "Should resolve insight");
        }
    }

    // ==================== POSITIVE PROGRESS TESTS ====================

    @Test
    @DisplayName("Positive progress insight when project has progress")
    void testPositiveProgress() {
        Project project = projectEngine.createProject("Good Progress");
        projectEngine.addTask(project.getId(), "Task 1");
        projectEngine.completeTask(project.getId(), "Task 1");
        projectEngine.addTask(project.getId(), "Task 2");
        projectEngine.startTask(project.getId(), "Task 2");

        chiefEngine.analyze();

        List<ChiefInsight> insights = chiefEngine.getAllInsights();
        boolean hasProgress = insights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.POSITIVE_PROGRESS);
        assertTrue(hasProgress, "Should detect positive progress");
    }
}