package com.darshan.agent.planning;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AutonomousPlanningEngine {

    private static final String PLANS_FILE = "execution_plans.json";
    private static final int MAX_DAILY_PRIORITIES = 3;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final GoalManager goalManager;
    private final ProjectIntelligenceEngine projectEngine;
    private final KnowledgeGraphEngine knowledgeGraph;
    private final ChiefOfStaffEngine chiefOfStaff;

    private final Map<String, ExecutionPlan> plans = new HashMap<>();
    private ExecutionPlan activePlan;

    public AutonomousPlanningEngine(GoalManager goalManager,
                                     ProjectIntelligenceEngine projectEngine,
                                     KnowledgeGraphEngine knowledgeGraph,
                                     ChiefOfStaffEngine chiefOfStaff) {
        this.goalManager = goalManager;
        this.projectEngine = projectEngine;
        this.knowledgeGraph = knowledgeGraph;
        this.chiefOfStaff = chiefOfStaff;
    }

    @PostConstruct
    public void init() {
        load();
    }

    // ==================== PLAN GENERATION ====================

    /**
     * Generate a complete execution plan from a goal.
     */
    public ExecutionPlan generatePlan(String goalDescription) {
        if (!goalManager.hasGoal() || !goalManager.getGoal().getDescription().equalsIgnoreCase(goalDescription)) {
            goalManager.createGoal(goalDescription);
        }

        AgentGoal goal = goalManager.getGoal();
        ExecutionPlan plan = new ExecutionPlan(goal.getDescription(), goal.getDescription());
        plan.setStatus(PlanStatus.IN_PROGRESS);

        // Decompose goal into milestones
        List<PlanMilestone> milestones = decomposeGoal(goalDescription);
        for (PlanMilestone milestone : milestones) {
            plan.getMilestones().add(milestone);
        }

        // Link to existing project if available
        linkToProject(plan);

        plans.put(plan.getId(), plan);
        activePlan = plan;
        save();
        return plan;
    }

    private List<PlanMilestone> decomposeGoal(String goal) {
        String lower = goal.toLowerCase();
        List<PlanMilestone> milestones = new ArrayList<>();

        if (lower.contains("java") && (lower.contains("backend") || lower.contains("developer"))) {
            milestones.add(createMilestone("Core Java", "Master Java fundamentals", 1, 7));
            milestones.add(createMilestone("JDBC", "Database connectivity with JDBC", 2, 14));
            milestones.add(createMilestone("Spring Boot", "Build REST APIs with Spring Boot", 3, 30));
            milestones.add(createMilestone("Projects", "Build 2-3 portfolio projects", 4, 45));
            milestones.add(createMilestone("Interview Prep", "Prepare for technical interviews", 5, 60));
        } else if (lower.contains("internship")) {
            milestones.add(createMilestone("Technical Foundation", "Core skills for the role", 1, 7));
            milestones.add(createMilestone("Projects", "Build relevant projects", 2, 21));
            milestones.add(createMilestone("Resume", "Prepare resume and portfolio", 3, 28));
            milestones.add(createMilestone("LinkedIn", "Optimize LinkedIn profile", 4, 30));
            milestones.add(createMilestone("Applications", "Apply to internships", 5, 35));
            milestones.add(createMilestone("Interview Prep", "Practice interviews", 6, 45));
        } else if (lower.contains("scc") || lower.contains("platform") || lower.contains("launch")) {
            milestones.add(createMilestone("Architecture", "Design system architecture", 1, 7));
            milestones.add(createMilestone("Backend", "Build backend services", 2, 21));
            milestones.add(createMilestone("Frontend", "Build user interface", 3, 35));
            milestones.add(createMilestone("Testing", "Write tests and QA", 4, 42));
            milestones.add(createMilestone("Deployment", "Deploy to production", 5, 49));
            milestones.add(createMilestone("Pilot Users", "Onboard first users", 6, 56));
        } else {
            // Generic goal decomposition
            milestones.add(createMilestone("Research", "Research and planning", 1, 7));
            milestones.add(createMilestone("Foundation", "Build foundational knowledge", 2, 14));
            milestones.add(createMilestone("Implementation", "Implement core features", 3, 30));
            milestones.add(createMilestone("Testing", "Test and refine", 4, 42));
            milestones.add(createMilestone("Launch", "Launch and iterate", 5, 56));
        }

        // Generate tasks for each milestone
        for (PlanMilestone milestone : milestones) {
            milestone.getTasks().addAll(generateTasksForMilestone(milestone));
        }

        return milestones;
    }

    private PlanMilestone createMilestone(String title, String description, int priority, int daysFromNow) {
        PlanMilestone milestone = new PlanMilestone(title, description, priority, LocalDate.now().plus(daysFromNow, ChronoUnit.DAYS));
        return milestone;
    }

    private List<ExecutionTask> generateTasksForMilestone(PlanMilestone milestone) {
        List<ExecutionTask> tasks = new ArrayList<>();
        String title = milestone.getTitle().toLowerCase();

        if (title.contains("core java") || title.contains("foundation")) {
            tasks.add(createTask("Learn Java syntax and basics", "Variables, loops, conditionals", ExecutionTask.Priority.HIGH, 8));
            tasks.add(createTask("Learn OOP concepts", "Classes, objects, inheritance, polymorphism", ExecutionTask.Priority.HIGH, 10));
            tasks.add(createTask("Learn Collections Framework", "List, Set, Map, Queue", ExecutionTask.Priority.MEDIUM, 6));
            tasks.add(createTask("Learn Multithreading", "Threads, synchronization, concurrency", ExecutionTask.Priority.MEDIUM, 8));
        } else if (title.contains("jdbc")) {
            tasks.add(createTask("Learn JDBC basics", "Drivers, connections, statements", ExecutionTask.Priority.HIGH, 4));
            tasks.add(createTask("Build CRUD application", "Create, Read, Update, Delete operations", ExecutionTask.Priority.HIGH, 6));
            tasks.add(createTask("Learn connection pooling", "HikariCP, Apache DBCP", ExecutionTask.Priority.MEDIUM, 3));
        } else if (title.contains("spring boot")) {
            tasks.add(createTask("Learn Spring Architecture", "IoC, DI, Beans", ExecutionTask.Priority.HIGH, 6));
            tasks.add(createTask("Build REST API", "Controllers, Request/Response", ExecutionTask.Priority.HIGH, 8));
            tasks.add(createTask("Learn JPA/Hibernate", "Entities, Relationships, Queries", ExecutionTask.Priority.HIGH, 8));
            tasks.add(createTask("Add Validation", "Bean validation, custom validators", ExecutionTask.Priority.MEDIUM, 4));
            tasks.add(createTask("Add Security", "Spring Security, JWT", ExecutionTask.Priority.HIGH, 6));
            tasks.add(createTask("Deploy Project", "Docker, cloud deployment", ExecutionTask.Priority.MEDIUM, 4));
        } else if (title.contains("project")) {
            tasks.add(createTask("Plan project architecture", "Design and document", ExecutionTask.Priority.HIGH, 4));
            tasks.add(createTask("Implement core features", "Build MVP", ExecutionTask.Priority.HIGH, 12));
            tasks.add(createTask("Add tests", "Unit and integration tests", ExecutionTask.Priority.MEDIUM, 6));
            tasks.add(createTask("Deploy to GitHub", "Push code, write README", ExecutionTask.Priority.MEDIUM, 2));
        } else if (title.contains("interview")) {
            tasks.add(createTask("Practice DSA", "LeetCode, HackerRank", ExecutionTask.Priority.HIGH, 15));
            tasks.add(createTask("Mock interviews", "Practice with peers", ExecutionTask.Priority.HIGH, 8));
            tasks.add(createTask("Review system design", "Common patterns", ExecutionTask.Priority.MEDIUM, 6));
        } else if (title.contains("resume")) {
            tasks.add(createTask("Draft resume", "Highlight skills and projects", ExecutionTask.Priority.HIGH, 4));
            tasks.add(createTask("Get feedback", "Peer review", ExecutionTask.Priority.MEDIUM, 2));
            tasks.add(createTask("Finalize resume", "Polish and format", ExecutionTask.Priority.HIGH, 2));
        } else if (title.contains("linkedin")) {
            tasks.add(createTask("Update headline", "Professional headline", ExecutionTask.Priority.MEDIUM, 1));
            tasks.add(createTask("Add projects", "Showcase work", ExecutionTask.Priority.MEDIUM, 2));
            tasks.add(createTask("Network", "Connect with professionals", ExecutionTask.Priority.LOW, 3));
        } else if (title.contains("application")) {
            tasks.add(createTask("Research companies", "Target list", ExecutionTask.Priority.HIGH, 3));
            tasks.add(createTask("Apply to 10 companies", "Submit applications", ExecutionTask.Priority.HIGH, 5));
            tasks.add(createTask("Track applications", "Spreadsheet or tool", ExecutionTask.Priority.MEDIUM, 2));
        } else {
            // Generic tasks
            tasks.add(createTask("Research topic", "Gather information", ExecutionTask.Priority.HIGH, 4));
            tasks.add(createTask("Learn fundamentals", "Core concepts", ExecutionTask.Priority.HIGH, 8));
            tasks.add(createTask("Practice", "Hands-on exercises", ExecutionTask.Priority.MEDIUM, 6));
            tasks.add(createTask("Build project", "Apply knowledge", ExecutionTask.Priority.HIGH, 10));
        }

        // Set dependencies
        setTaskDependencies(tasks);
        return tasks;
    }

    private ExecutionTask createTask(String title, String description, ExecutionTask.Priority priority, double hours) {
        return new ExecutionTask(title, description, priority, hours);
    }

    private void setTaskDependencies(List<ExecutionTask> tasks) {
        for (int i = 1; i < tasks.size(); i++) {
            tasks.get(i).getDependencies().add(tasks.get(i - 1).getId());
        }
    }

    private void linkToProject(ExecutionPlan plan) {
        // Find matching project by goal name
        Optional<com.darshan.agent.project.Project> project = projectEngine.findProjectByName(plan.getGoalName());
        if (project.isPresent()) {
            // Could store planId in project metadata
        }
    }

    // ==================== TASK COMPLETION ====================

    /**
     * Mark a task as complete and update plan progress.
     */
    public boolean completeTask(String planId, String taskId) {
        ExecutionPlan plan = plans.get(planId);
        if (plan == null) return false;

        for (PlanMilestone milestone : plan.getMilestones()) {
            for (ExecutionTask task : milestone.getTasks()) {
                if (task.getId().equals(taskId) && !task.isCompleted()) {
                    task.complete();
                    plan.setUpdatedAt(Instant.now());
                    save();
                    return true;
                }
            }
        }
        return false;
    }

    // ==================== DAILY PRIORITIES ====================

    /**
     * Get top 3 priorities for today.
     */
    public List<ExecutionTask> getDailyPriorities() {
        if (activePlan == null) return List.of();

        List<ExecutionTask> allTasks = activePlan.getAllTasks();
        return allTasks.stream()
                .filter(t -> !t.isCompleted() && !t.isBlocked())
                .filter(t -> areDependenciesMet(t, allTasks))
                .sorted((a, b) -> {
                    int priorityCompare = Integer.compare(getPriorityWeight(b.getPriority()), getPriorityWeight(a.getPriority()));
                    if (priorityCompare != 0) return priorityCompare;
                    return Double.compare(b.getEstimatedHours(), a.getEstimatedHours());
                })
                .limit(MAX_DAILY_PRIORITIES)
                .collect(Collectors.toList());
    }

    private boolean areDependenciesMet(ExecutionTask task, List<ExecutionTask> allTasks) {
        Map<String, ExecutionTask> taskMap = allTasks.stream()
                .collect(Collectors.toMap(ExecutionTask::getId, t -> t));
        for (String depId : task.getDependencies()) {
            ExecutionTask dep = taskMap.get(depId);
            if (dep == null || !dep.isCompleted()) {
                return false;
            }
        }
        return true;
    }

    private int getPriorityWeight(ExecutionTask.Priority priority) {
        return switch (priority) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    // ==================== PLAN REVIEW ====================

    /**
     * Review plan and detect issues.
     */
    public PlanReview reviewPlan() {
        PlanReview review = new PlanReview();
        if (activePlan == null) {
            review.addIssue("No active plan", "Create a plan first");
            return review;
        }

        // Check for blocked tasks
        long blockedCount = activePlan.getAllTasks().stream().filter(ExecutionTask::isBlocked).count();
        if (blockedCount > 0) {
            review.addIssue(blockedCount + " blocked tasks", "Resolve dependencies or blockers");
        }

        // Check for too many open tasks
        long openTasks = activePlan.getAllTasks().stream().filter(t -> !t.isCompleted()).count();
        if (openTasks > 20) {
            review.addIssue("Too many open tasks (" + openTasks + ")", "Focus on completing existing tasks");
        }

        // Check for overdue milestones
        LocalDate today = LocalDate.now();
        for (PlanMilestone milestone : activePlan.getMilestones()) {
            if (milestone.getTargetDate() != null && milestone.getTargetDate().isBefore(today) && milestone.getProgress() < 100) {
                review.addIssue("Overdue milestone: " + milestone.getTitle(), "Update target date or accelerate progress");
            }
        }

        // Check progress
        double progress = activePlan.getOverallProgress();
        if (progress < 10 && activePlan.getCreatedAt().plus(7, ChronoUnit.DAYS).isBefore(Instant.now())) {
            review.addIssue("Plan stagnation", "Less than 10% progress after 7 days");
        }

        return review;
    }

    // ==================== QUERIES ====================

    public Optional<ExecutionPlan> getActivePlan() {
        return Optional.ofNullable(activePlan);
    }

    public List<ExecutionPlan> getAllPlans() {
        return new ArrayList<>(plans.values());
    }

    public Optional<ExecutionPlan> getPlan(String planId) {
        return Optional.ofNullable(plans.get(planId));
    }

    public List<ExecutionTask> getDailyPrioritiesForPlan(String planId) {
        ExecutionPlan plan = plans.get(planId);
        if (plan == null) return List.of();

        List<ExecutionTask> allTasks = plan.getAllTasks();
        return allTasks.stream()
                .filter(t -> !t.isCompleted() && !t.isBlocked())
                .filter(t -> areDependenciesMet(t, allTasks))
                .sorted((a, b) -> Integer.compare(getPriorityWeight(b.getPriority()), getPriorityWeight(a.getPriority())))
                .limit(MAX_DAILY_PRIORITIES)
                .collect(Collectors.toList());
    }

    public String getPlanSummary() {
        if (activePlan == null) return "No active plan";
        return String.format("Goal: %s | Progress: %.0f%% | Tasks: %d/%d",
                activePlan.getGoalName(),
                activePlan.getOverallProgress(),
                activePlan.getCompletedTasks(),
                activePlan.getTotalTasks());
    }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("plans", new ArrayList<>(plans.values()));
            data.put("activePlanId", activePlan != null ? activePlan.getId() : null);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(PLANS_FILE), data);
        } catch (IOException e) {
            System.err.println("[Planning] Failed to save: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        try {
            File file = new File(PLANS_FILE);
            if (!file.exists()) return;

            Map<String, Object> data = mapper.readValue(file, new TypeReference<Map<String, Object>>() {});

            if (data.containsKey("plans")) {
                List<ExecutionPlan> loaded = mapper.convertValue(data.get("plans"), new TypeReference<List<ExecutionPlan>>() {});
                for (ExecutionPlan p : loaded) {
                    plans.put(p.getId(), p);
                }
            }

            if (data.containsKey("activePlanId")) {
                String activeId = (String) data.get("activePlanId");
                activePlan = plans.get(activeId);
            }

            System.out.println("[Planning] Loaded " + plans.size() + " plans");
        } catch (IOException e) {
            System.err.println("[Planning] Failed to load: " + e.getMessage());
        }
    }

    // ==================== INNER CLASS ====================

    public static class PlanReview {
        private final List<String> issues = new ArrayList<>();
        private final List<String> recommendations = new ArrayList<>();

        public void addIssue(String issue, String recommendation) {
            issues.add(issue);
            recommendations.add(recommendation);
        }

        public boolean hasIssues() { return !issues.isEmpty(); }
        public List<String> getIssues() { return issues; }
        public List<String> getRecommendations() { return recommendations; }
    }
}