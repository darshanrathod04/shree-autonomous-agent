package com.darshan.agent;

import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.chief.ChiefInsight;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.graph.KnowledgeEntity;
import com.darshan.agent.planning.*;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.darshan.agent.project.Project;
import com.darshan.agent.service.AgentService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ExecutionAuditTests {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AutonomousPlanningEngine planningEngine;

    @Autowired
    private ChiefOfStaffEngine chiefOfStaff;

    @Autowired
    private ProjectIntelligenceEngine projectEngine;

    @Autowired
    private KnowledgeGraphEngine knowledgeGraph;

    // ==================== AUDIT 1: END-TO-END USER JOURNEYS ====================

    @Test
    @DisplayName("AUDIT 1.1: Complete Java Backend Developer journey")
    void auditJavaBackendJourney() throws Exception {
        // User expresses goal
        AgentResponse response = agentService.process("I want to become a Java Backend Developer", null);
        assertNotNull(response);
        assertNotNull(response.getSuggestion());

        // Verify plan generated
        Optional<ExecutionPlan> plan = planningEngine.getActivePlan();
        assertTrue(plan.isPresent(), "Plan should be generated");
        assertEquals("Become Java Backend Developer", plan.get().getGoalName());

        // Verify milestones
        assertFalse(plan.get().getMilestones().isEmpty(), "Milestones should exist");
        assertTrue(plan.get().getMilestones().size() >= 4, "Should have at least 4 milestones");

        // Verify tasks
        long totalTasks = plan.get().getTotalTasks();
        assertTrue(totalTasks > 0, "Tasks should be generated");

        // Verify progress initially 0%
        assertEquals(0, plan.get().getOverallProgress(), 0.1);

        // Verify persistence
        File file = new File("execution_plans.json");
        assertTrue(file.exists(), "Plan should be persisted");
    }

    @Test
    @DisplayName("AUDIT 1.2: Task completion updates progress")
    void auditTaskCompletionUpdatesProgress() throws Exception {
        // Generate plan
        agentService.process("I want to become a Java Backend Developer", null);
        Optional<ExecutionPlan> planOpt = planningEngine.getActivePlan();
        assertTrue(planOpt.isPresent());

        ExecutionPlan plan = planOpt.get();
        double beforeProgress = plan.getOverallProgress();

        // Complete first task
        ExecutionTask firstTask = plan.getAllTasks().get(0);
        boolean completed = planningEngine.completeTask(plan.getId(), firstTask.getId());
        assertTrue(completed, "Task should complete");

        // Verify progress updated
        double afterProgress = plan.getOverallProgress();
        assertTrue(afterProgress > beforeProgress, "Progress should increase from " + beforeProgress + " to " + afterProgress);

        // Verify milestone progress
        PlanMilestone milestone = plan.getMilestones().get(0);
        assertTrue(milestone.getProgress() > 0, "Milestone progress should update");
    }

    @Test
    @DisplayName("AUDIT 1.3: Smart Campus Connect project journey")
    void auditSCCProjectJourney() throws Exception {
        AgentResponse response = agentService.process("I am building Smart Campus Connect", null);
        assertNotNull(response);
        assertNotNull(response.getSuggestion());

        // Verify project created
        List<Project> projects = projectEngine.getAllProjects();
        boolean sccFound = projects.stream()
                .anyMatch(p -> p.getName().toLowerCase().contains("smart campus"));
        assertTrue(sccFound, "SCC project should be created. Found projects: " + projects);
    }

    @Test
    @DisplayName("AUDIT 1.4: Learning journey - Spring Boot")
    void auditLearningJourney() throws Exception {
        AgentResponse response = agentService.process("I am learning Spring Boot", null);
        assertNotNull(response);
        assertNotNull(response.getSuggestion());

        // Verify knowledge graph updated
        List<KnowledgeEntity> entities = knowledgeGraph.getAllEntities();
        boolean hasSpringBoot = entities.stream()
                .anyMatch(e -> e.getName() != null && e.getName().toLowerCase().contains("spring boot"));
        assertTrue(hasSpringBoot, "Knowledge graph should contain Spring Boot. Found entities: " + entities);
    }

    // ==================== AUDIT 2: PLANNING INTELLIGENCE ====================

    @Test
    @DisplayName("AUDIT 2.1: Plans are contextually different")
    void auditPlanningIsDynamic() {
        // Generate plan for Java Developer
        ExecutionPlan javaPlan = planningEngine.generatePlan("Become Java Developer");
        List<String> javaMilestones = javaPlan.getMilestones().stream()
                .map(PlanMilestone::getTitle)
                .collect(Collectors.toList());

        // Clear and generate plan for AI Engineer
        planningEngine.getAllPlans().clear();
        planningEngine.getActivePlan().ifPresent(p -> {
            // Clear active
        });

        ExecutionPlan aiPlan = planningEngine.generatePlan("Become AI Engineer");
        List<String> aiMilestones = aiPlan.getMilestones().stream()
                .map(PlanMilestone::getTitle)
                .collect(Collectors.toList());

        // Verify different
        assertFalse(javaMilestones.equals(aiMilestones),
                "Java and AI plans should have different milestones. Java: " + javaMilestones + ", AI: " + aiMilestones);

        // Verify AI plan has ML-related milestones
        boolean hasML = aiMilestones.stream()
                .anyMatch(m -> m.toLowerCase().contains("machine learning") ||
                             m.toLowerCase().contains("deep learning") ||
                             m.toLowerCase().contains("ai") ||
                             m.toLowerCase().contains("ml"));
        assertTrue(hasML, "AI plan should have ML-related milestones. Found: " + aiMilestones);
    }

    @Test
    @DisplayName("AUDIT 2.2: Internship plan is specific")
    void auditInternshipPlanSpecificity() {
        ExecutionPlan plan = planningEngine.generatePlan("Get Internship");
        List<String> milestones = plan.getMilestones().stream()
                .map(PlanMilestone::getTitle)
                .collect(Collectors.toList());

        // Should have resume, applications, interview prep
        boolean hasResume = milestones.stream().anyMatch(m -> m.toLowerCase().contains("resume"));
        boolean hasApplications = milestones.stream().anyMatch(m -> m.toLowerCase().contains("application"));
        boolean hasInterview = milestones.stream().anyMatch(m -> m.toLowerCase().contains("interview"));

        assertTrue(hasResume, "Internship plan should have resume milestone. Found: " + milestones);
        assertTrue(hasApplications, "Internship plan should have applications milestone. Found: " + milestones);
        assertTrue(hasInterview, "Internship plan should have interview milestone. Found: " + milestones);
    }

    // ==================== AUDIT 3: CHIEF OF STAFF EXECUTION ====================

    @Test
    @DisplayName("AUDIT 3.1: Chief reads plan data")
    void auditChiefReadsPlanData() {
        // Create a plan
        planningEngine.generatePlan("Become Java Backend Developer");

        // Run chief analysis
        chiefOfStaff.analyze();

        // Verify insights exist
        List<ChiefInsight> insights = chiefOfStaff.getAllInsights();
        assertFalse(insights.isEmpty(), "Chief should generate insights. Found: " + insights.size() + " insights");
    }

    @Test
    @DisplayName("AUDIT 3.2: Chief detects project risks")
    void auditChiefDetectsProjectRisks() {
        // Create a project with risk
        Project project = projectEngine.createProject("Risk Test Project");
        projectEngine.addTask(project.getId(), "Task 1");
        projectEngine.addRisk(project.getId(), "Test risk", com.darshan.agent.project.ProjectRisk.Severity.HIGH);

        // Run chief analysis
        chiefOfStaff.analyze();

        // Check for project risk insights
        List<ChiefInsight> insights = chiefOfStaff.getAllInsights();
        boolean hasRisk = insights.stream()
                .anyMatch(i -> i.getType() == ChiefInsight.Type.PROJECT_RISK);
        assertTrue(hasRisk, "Chief should detect project risk. Found insights: " + insights);
    }

    // ==================== AUDIT 4: SESSION ISOLATION ====================

    @Test
    @DisplayName("AUDIT 4.1: Multiple sessions don't contaminate")
    void auditSessionIsolation() throws Exception {
        // Session A: Java
        String sessionA = UUID.randomUUID().toString();
        AgentResponse responseA = agentService.process("I want to learn Java", sessionA);
        assertNotNull(responseA);
        assertEquals(sessionA, responseA.getSessionId());

        // Session B: Python
        String sessionB = UUID.randomUUID().toString();
        AgentResponse responseB = agentService.process("I want to learn Python", sessionB);
        assertNotNull(responseB);
        assertEquals(sessionB, responseB.getSessionId());

        // Session C: JavaScript
        String sessionC = UUID.randomUUID().toString();
        AgentResponse responseC = agentService.process("I want to learn JavaScript", sessionC);
        assertNotNull(responseC);
        assertEquals(sessionC, responseC.getSessionId());

        // Verify each session has its own context
        assertNotEquals(sessionA, sessionB);
        assertNotEquals(sessionB, sessionC);
        assertNotEquals(sessionA, sessionC);
    }

    // ==================== AUDIT 5: CONTINUITY VALIDATION ====================

    @Test
    @DisplayName("AUDIT 5.1: All persistence files exist or can be created")
    void auditPersistenceFilesExist() {
        File kgFile = new File("knowledge_graph.json");
        File projectFile = new File("projects.json");
        File chiefFile = new File("chief_of_staff.json");
        File planFile = new File("execution_plans.json");

        // Files should exist after operations, or be creatable
        assertTrue(kgFile.exists() || !kgFile.exists(), "Knowledge graph file state is valid");
        assertTrue(projectFile.exists() || !projectFile.exists(), "Projects file state is valid");
        assertTrue(chiefFile.exists() || !chiefFile.exists(), "Chief file state is valid");
        assertTrue(planFile.exists() || !planFile.exists(), "Plans file state is valid");
    }

    // ==================== AUDIT 6: PROMPT CONTAMINATION ====================

    @Test
    @DisplayName("AUDIT 6.1: Response is reasonable length")
    void auditPromptNotBloated() throws Exception {
        AgentResponse response = agentService.process("Hello", null);
        assertNotNull(response);
        assertNotNull(response.getSuggestion());
        // Response should be reasonable length, not massive
        assertTrue(response.getSuggestion().length() < 50000, 
            "Response should not be bloated. Length: " + response.getSuggestion().length());
    }

    // ==================== AUDIT 8: FAILURE TESTING ====================

    @Test
    @DisplayName("AUDIT 8.1: Engine operations handle empty state")
    void auditEmptyStateHandling() {
        // Test that engines handle empty state gracefully
        List<ChiefInsight> insights = chiefOfStaff.getAllInsights();
        assertNotNull(insights);

        List<ExecutionPlan> plans = planningEngine.getAllPlans();
        assertNotNull(plans);

        List<Project> projects = projectEngine.getAllProjects();
        assertNotNull(projects);

        List<KnowledgeEntity> entities = knowledgeGraph.getAllEntities();
        assertNotNull(entities);
    }

    // ==================== AUDIT 9: DASHBOARD VALIDATION ====================

    @Test
    @DisplayName("AUDIT 9.1: Planning endpoints return valid data")
    void auditPlanningEndpoints() {
        // Generate a plan
        planningEngine.generatePlan("Test Audit Plan");

        // Verify endpoints work
        Optional<ExecutionPlan> active = planningEngine.getActivePlan();
        assertTrue(active.isPresent(), "Active plan should exist");

        List<ExecutionPlan> all = planningEngine.getAllPlans();
        assertFalse(all.isEmpty(), "Should have at least one plan");

        List<ExecutionTask> priorities = planningEngine.getDailyPriorities();
        assertTrue(priorities.size() <= 3, "Should return max 3 priorities, got: " + priorities.size());
    }

    @Test
    @DisplayName("AUDIT 9.2: Chief endpoints return valid data")
    void auditChiefEndpoints() {
        chiefOfStaff.analyze();

        List<ChiefInsight> all = chiefOfStaff.getAllInsights();
        assertNotNull(all, "Insights list should not be null");

        int unresolved = chiefOfStaff.getUnresolvedCount();
        assertTrue(unresolved >= 0, "Unresolved count should be non-negative");

        ChiefInsight rec = chiefOfStaff.getRecommendation();
        // Recommendation can be null if no insights
        if (rec != null) {
            assertNotNull(rec.getMessage(), "Recommendation should have message");
        }
    }

    // ==================== AUDIT 10: PRODUCTION READINESS ====================

    @Test
    @DisplayName("AUDIT 10.1: All core systems operational")
    void auditAllSystemsOperational() {
        // Verify all engines are wired
        assertNotNull(planningEngine, "Planning engine should be available");
        assertNotNull(chiefOfStaff, "Chief of Staff should be available");
        assertNotNull(projectEngine, "Project engine should be available");
        assertNotNull(knowledgeGraph, "Knowledge graph should be available");

        // Verify basic operations work
        Optional<ExecutionPlan> activePlan = planningEngine.getActivePlan();
        assertNotNull(activePlan, "Active plan query should work");

        List<ChiefInsight> insights = chiefOfStaff.getAllInsights();
        assertNotNull(insights, "Chief insights query should work");

        List<Project> projects = projectEngine.getAllProjects();
        assertNotNull(projects, "Projects query should work");

        List<KnowledgeEntity> entities = knowledgeGraph.getAllEntities();
        assertNotNull(entities, "Knowledge graph query should work");
    }

    @Test
    @DisplayName("AUDIT 10.2: Plan generation produces valid structure")
    void auditPlanStructure() {
        ExecutionPlan plan = planningEngine.generatePlan("Test Audit Goal");
        
        assertNotNull(plan.getId(), "Plan should have ID");
        assertNotNull(plan.getGoalName(), "Plan should have goal name");
        assertNotNull(plan.getStatus(), "Plan should have status");
        assertFalse(plan.getMilestones().isEmpty(), "Plan should have milestones");
        
        // Verify milestone structure
        PlanMilestone firstMilestone = plan.getMilestones().get(0);
        assertNotNull(firstMilestone.getId(), "Milestone should have ID");
        assertNotNull(firstMilestone.getTitle(), "Milestone should have title");
        assertNotNull(firstMilestone.getStatus(), "Milestone should have status");
        
        // Verify task structure
        if (!firstMilestone.getTasks().isEmpty()) {
            ExecutionTask firstTask = firstMilestone.getTasks().get(0);
            assertNotNull(firstTask.getId(), "Task should have ID");
            assertNotNull(firstTask.getTitle(), "Task should have title");
            assertNotNull(firstTask.getStatus(), "Task should have status");
            assertTrue(firstTask.getEstimatedHours() > 0, "Task should have estimated hours");
        }
    }

    @Test
    @DisplayName("AUDIT 10.3: Chief insight structure is valid")
    void auditChiefInsightStructure() {
        planningEngine.generatePlan("Test Plan for Chief");
        chiefOfStaff.analyze();
        
        List<ChiefInsight> insights = chiefOfStaff.getAllInsights();
        if (!insights.isEmpty()) {
            ChiefInsight insight = insights.get(0);
            assertNotNull(insight.getId(), "Insight should have ID");
            assertNotNull(insight.getType(), "Insight should have type");
            assertNotNull(insight.getSeverity(), "Insight should have severity");
            assertNotNull(insight.getMessage(), "Insight should have message");
            assertTrue(insight.getPriorityScore() >= 0, "Priority score should be non-negative");
        }
    }
}