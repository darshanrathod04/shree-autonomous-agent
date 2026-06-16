package com.darshan.agent;

import com.darshan.agent.project.*;
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
class ProjectIntelligenceTests {

    @Autowired
    private ProjectIntelligenceEngine projectEngine;

    @BeforeEach
    void setUp() {
        projectEngine.getAllProjects().forEach(p -> projectEngine.removeProject(p.getId()));
    }

    // ==================== PROJECT TESTS ====================

    @Test
    @DisplayName("Create project with name and description")
    void testProjectCreation() {
        Project project = projectEngine.createProject("Shree AI", "Autonomous AI Agent");
        assertNotNull(project);
        assertEquals("Shree AI", project.getName());
        assertEquals("Autonomous AI Agent", project.getDescription());
        assertEquals(ProjectStatus.PLANNING, project.getStatus());
    }

    @Test
    @DisplayName("Find project by name")
    void testFindProject() {
        projectEngine.createProject("Test Project");
        Optional<Project> found = projectEngine.findProjectByName("Test Project");
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Duplicate project returns existing")
    void testDuplicateProject() {
        Project p1 = projectEngine.createProject("Duplicate Test");
        Project p2 = projectEngine.createProject("Duplicate Test");
        assertEquals(p1.getId(), p2.getId());
    }

    // ==================== TASK TESTS ====================

    @Test
    @DisplayName("Add task to project")
    void testAddTask() {
        Project project = projectEngine.createProject("Project A");
        ProjectTask task = projectEngine.addTask(project.getId(), "Implement feature X", ProjectTask.Priority.HIGH);
        assertNotNull(task);
        assertEquals("Implement feature X", task.getTitle());
        assertEquals(ProjectTask.Status.TODO, task.getStatus());
        assertEquals(ProjectTask.Priority.HIGH, task.getPriority());
    }

    @Test
    @DisplayName("Complete task")
    void testCompleteTask() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addTask(project.getId(), "Task 1");
        boolean completed = projectEngine.completeTask(project.getId(), "Task 1");
        assertTrue(completed);
        assertEquals(ProjectTask.Status.DONE, project.getTasks().get(0).getStatus());
    }

    @Test
    @DisplayName("Start task sets in-progress")
    void testStartTask() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addTask(project.getId(), "Task 1");
        boolean started = projectEngine.startTask(project.getId(), "Task 1");
        assertTrue(started);
        assertEquals(ProjectTask.Status.IN_PROGRESS, project.getTasks().get(0).getStatus());
    }

    // ==================== MILESTONE TESTS ====================

    @Test
    @DisplayName("Add milestone to project")
    void testAddMilestone() {
        Project project = projectEngine.createProject("Project A");
        ProjectMilestone milestone = projectEngine.addMilestone(project.getId(), "Alpha release");
        assertNotNull(milestone);
        assertEquals("Alpha release", milestone.getTitle());
        assertFalse(milestone.isCompleted());
    }

    @Test
    @DisplayName("Complete milestone")
    void testCompleteMilestone() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addMilestone(project.getId(), "Beta");
        boolean completed = projectEngine.completeMilestone(project.getId(), "Beta");
        assertTrue(completed);
        assertTrue(project.getMilestones().get(0).isCompleted());
    }

    // ==================== DECISION TESTS ====================

    @Test
    @DisplayName("Add decision to project")
    void testAddDecision() {
        Project project = projectEngine.createProject("Project A");
        ProjectDecision decision = projectEngine.addDecision(project.getId(), "Use Spring Boot", "Best fit for microservices");
        assertNotNull(decision);
        assertEquals("Use Spring Boot", decision.getDecision());
    }

    // ==================== RISK TESTS ====================

    @Test
    @DisplayName("Add risk to project")
    void testAddRisk() {
        Project project = projectEngine.createProject("Project A");
        ProjectRisk risk = projectEngine.addRisk(project.getId(), "Authentication delay", ProjectRisk.Severity.HIGH);
        assertNotNull(risk);
        assertEquals("Authentication delay", risk.getTitle());
        assertEquals(ProjectRisk.Severity.HIGH, risk.getSeverity());
        assertFalse(risk.isResolved());
    }

    @Test
    @DisplayName("Resolve risk")
    void testResolveRisk() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addRisk(project.getId(), "Test risk", ProjectRisk.Severity.MEDIUM);
        boolean resolved = projectEngine.resolveRisk(project.getId(), "Test risk");
        assertTrue(resolved);
        assertTrue(project.getRisks().get(0).isResolved());
    }

    // ==================== PROGRESS CALCULATION TESTS ====================

    @Test
    @DisplayName("Progress calculation: 0% when no tasks")
    void testProgressEmpty() {
        Project project = projectEngine.createProject("Empty Project");
        assertEquals(0, project.getProgressPercentage());
    }

    @Test
    @DisplayName("Progress calculation: mixed tasks")
    void testProgressMixed() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addTask(project.getId(), "Task 1");  // TODO = 0%
        projectEngine.addTask(project.getId(), "Task 2");  // TODO = 0%
        assertEquals(0, project.getProgressPercentage(), "All TODO = 0%");

        projectEngine.completeTask(project.getId(), "Task 1");  // DONE = 100%
        // 1/2 done = 50%
        assertEquals(50, project.getProgressPercentage(), "1/2 done = 50%");

        projectEngine.completeTask(project.getId(), "Task 2");
        assertEquals(100, project.getProgressPercentage(), "All done = 100%");
    }

    // ==================== PERSISTENCE TESTS ====================

    @Test
    @DisplayName("Project persists to file")
    void testPersistence() {
        projectEngine.createProject("Persistent Project");
        File file = new File("projects.json");
        assertTrue(file.exists());
    }

    @Test
    @DisplayName("Project survives restart")
    void testSurvivesRestart() {
        projectEngine.createProject("Survive Project");
        projectEngine.save();

        projectEngine.getAllProjects().forEach(p -> projectEngine.removeProject(p.getId()));
        projectEngine.load();

        Optional<Project> found = projectEngine.findProjectByName("Survive Project");
        assertTrue(found.isPresent(), "Project should survive restart via file persistence");
    }

    // ==================== QUERY TESTS ====================

    @Test
    @DisplayName("Get pending tasks")
    void testGetPendingTasks() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addTask(project.getId(), "Task 1");
        projectEngine.addTask(project.getId(), "Task 2");
        projectEngine.completeTask(project.getId(), "Task 1");

        List<ProjectTask> pending = projectEngine.getPendingTasks(project.getId());
        assertEquals(1, pending.size());
        assertEquals("Task 2", pending.get(0).getTitle());
    }

    @Test
    @DisplayName("Get unresolved risks")
    void testGetUnresolvedRisks() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addRisk(project.getId(), "Risk 1", ProjectRisk.Severity.HIGH);
        projectEngine.addRisk(project.getId(), "Risk 2", ProjectRisk.Severity.LOW);
        projectEngine.resolveRisk(project.getId(), "Risk 1");

        List<ProjectRisk> risks = projectEngine.getUnresolvedRisks(project.getId());
        assertEquals(1, risks.size());
        assertEquals("Risk 2", risks.get(0).getTitle());
    }

    @Test
    @DisplayName("Get project decisions")
    void testGetDecisions() {
        Project project = projectEngine.createProject("Project A");
        projectEngine.addDecision(project.getId(), "Decision 1", "Reason 1");
        projectEngine.addDecision(project.getId(), "Decision 2", "Reason 2");

        List<ProjectDecision> decisions = projectEngine.getDecisions(project.getId());
        assertEquals(2, decisions.size());
    }

    // ==================== SUMARY TESTS ====================

    @Test
    @DisplayName("Project summary format")
    void testProjectSummary() {
        Project project = projectEngine.createProject("Shree AI");
        projectEngine.addTask(project.getId(), "Task 1");
        projectEngine.addTask(project.getId(), "Task 2");
        projectEngine.completeTask(project.getId(), "Task 1");

        String summary = project.getSummary();
        assertTrue(summary.contains("Shree AI"));
        assertTrue(summary.contains("50%"));
        assertTrue(summary.contains("1/2"));
    }

    @Test
    @DisplayName("Status summary for multiple projects")
    void testStatusSummary() {
        projectEngine.createProject("Project A");
        projectEngine.createProject("Project B");
        String summary = projectEngine.getStatusSummary();
        assertTrue(summary.contains("Project A") || summary.contains("Project B"));
    }

    // ==================== EXTRACTION TESTS ====================

    @Test
    @DisplayName("Extract project from building input")
    void testExtractProject() {
        projectEngine.extractFromInput("I am building MyApp");
        Optional<Project> project = projectEngine.findProjectByName("MyApp");
        assertTrue(project.isPresent());
    }

    @Test
    @DisplayName("Extract task completion")
    void testExtractTaskCompletion() {
        Project project = projectEngine.createProject("MyApp");
        projectEngine.addTask(project.getId(), "Session isolation");

        projectEngine.extractFromInput("I completed Session isolation");
        assertEquals(ProjectTask.Status.DONE, project.getTasks().get(0).getStatus());
    }

    // ==================== DASHBOARD TESTS ====================

    @Test
    @DisplayName("Project count accurate")
    void testProjectCount() {
        projectEngine.createProject("P1");
        projectEngine.createProject("P2");
        assertTrue(projectEngine.getProjectCount() >= 2);
    }

    @Test
    @DisplayName("Total task count accurate")
    void testTotalTaskCount() {
        Project p = projectEngine.createProject("P1");
        projectEngine.addTask(p.getId(), "T1");
        projectEngine.addTask(p.getId(), "T2");
        assertEquals(2, projectEngine.getTotalTaskCount());
    }
}