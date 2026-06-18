package com.darshan.agent.chief;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.graph.KnowledgeEntity;
import com.darshan.agent.project.*;
import com.darshan.agent.context.LessonState;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class ChiefOfStaffEngine {

    private static final String INSIGHTS_FILE = "chief_of_staff.json";
    private static final int STAGNATION_DAYS = 14;
    private static final int GOAL_IDLE_DAYS = 10;
    private static final int LEARNING_IDLE_DAYS = 7;
    private static final int TASK_OVERLOAD_THRESHOLD = 40;
    private static final int PROMPT_MAX_INSIGHTS = 3;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final KnowledgeGraphEngine knowledgeGraph;
    private final ProjectIntelligenceEngine projectEngine;
    private final GoalManager goalManager;

    private final List<ChiefInsight> insights = Collections.synchronizedList(new ArrayList<>());
    private ChiefInsight currentRecommendation;

    public ChiefOfStaffEngine(KnowledgeGraphEngine knowledgeGraph,
                               ProjectIntelligenceEngine projectEngine,
                               GoalManager goalManager) {
        this.knowledgeGraph = knowledgeGraph;
        this.projectEngine = projectEngine;
        this.goalManager = goalManager;
    }

    @PostConstruct
    public void init() {
        load();
        analyze();
    }

    // ==================== ANALYSIS ====================

    /**
     * Run full analysis of all available data.
     * Generates insights and top recommendation.
     */
    public synchronized void analyze() {
        insights.clear();
        List<ChiefInsight> newInsights = new ArrayList<>();

        // Analyze projects
        for (Project project : projectEngine.getAllProjects()) {
            analyzeProject(project).ifPresent(newInsights::add);
        }

        // Analyze goals
        analyzeGoals().ifPresent(newInsights::add);

        // Analyze learning
        analyzeLearning().ifPresent(newInsights::add);

        // Analyze task overload
        analyzeTaskOverload().ifPresent(newInsights::add);

        // Analyze positive progress
        analyzePositiveProgress().ifPresent(newInsights::add);

        insights.addAll(newInsights);
        currentRecommendation = generateRecommendation(newInsights);
        save();
    }

    private Optional<ChiefInsight> analyzeProject(Project project) {
        List<ProjectTask> allTasks = project.getTasks();

        if (allTasks.isEmpty()) return Optional.empty();

        long doneCount = allTasks.stream().filter(t -> t.getStatus() == ProjectTask.Status.DONE).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == ProjectTask.Status.IN_PROGRESS).count();
        long unresolvedRisks = project.getUnresolvedRiskCount();

        // Check project stagnation: no tasks done in 14 days
        if (doneCount == 0 && project.getCreatedAt().plus(STAGNATION_DAYS, ChronoUnit.DAYS).isBefore(Instant.now())) {
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.PROJECT_STAGNATION,
                    ChiefInsight.Severity.HIGH,
                    90,
                    "Project '" + project.getName() + "' has no completed tasks in " + STAGNATION_DAYS + " days",
                    "Start with one small task on " + project.getName() + " to break stagnation"
            ));
        }

        // Check active risks
        if (unresolvedRisks > 0) {
            String riskNames = project.getRisks().stream()
                    .filter(r -> !r.isResolved())
                    .map(ProjectRisk::getTitle)
                    .limit(2)
                    .collect(Collectors.joining(", "));
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.PROJECT_RISK,
                    unresolvedRisks > 2 ? ChiefInsight.Severity.HIGH : ChiefInsight.Severity.MEDIUM,
                    80 + (int)unresolvedRisks * 5,
                    "Project '" + project.getName() + "' has " + unresolvedRisks + " unresolved risks: " + riskNames,
                    "Resolve " + riskNames + " to unblock progress on " + project.getName()
            ));
        }

        // Check progress
        if (doneCount > 0 && inProgressCount > 0) {
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.POSITIVE_PROGRESS,
                    ChiefInsight.Severity.LOW,
                    10,
                    "Project '" + project.getName() + "' is progressing (" + (int)project.getProgressPercentage() + "%)",
                    "Keep the momentum on " + project.getName()
            ));
        }

        return Optional.empty();
    }

    private Optional<ChiefInsight> analyzeGoals() {
        if (!goalManager.hasGoal()) return Optional.empty();

        AgentGoal goal = goalManager.getGoal();
        if (goal == null) return Optional.empty();

        long completed = goal.getSubGoals().stream().filter(g -> g.isCompleted()).count();
        long total = goal.getSubGoals().size();

        if (total > 0 && completed == 0) {
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.GOAL_DELAY,
                    ChiefInsight.Severity.HIGH,
                    100,
                    "Goal '" + goal.getDescription() + "' has no completed steps (" + total + " pending)",
                    "Define and complete the first step toward: " + goal.getDescription()
            ));
        }

        if (total > 0 && completed < total) {
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.GOAL_DELAY,
                    ChiefInsight.Severity.MEDIUM,
                    70,
                    "Goal '" + goal.getDescription() + "' is " + (completed * 100 / total) + "% complete",
                    "Continue working on the next step toward your goal"
            ));
        }

        return Optional.empty();
    }

    private Optional<ChiefInsight> analyzeLearning() {
        List<KnowledgeEntity> learningTopics = knowledgeGraph.getEntitiesByType(
                com.darshan.agent.graph.EntityType.LEARNING_TOPIC);
        if (learningTopics.isEmpty()) return Optional.empty();

        // No learning activity can be detected without session data
        // For now, signal that learning topics exist
        return Optional.of(new ChiefInsight(
                ChiefInsight.Type.LEARNING_GAP,
                ChiefInsight.Severity.MEDIUM,
                70,
                learningTopics.size() + " learning topics tracked: " +
                        learningTopics.stream().map(KnowledgeEntity::getName).limit(3).collect(Collectors.joining(", ")),
                "Review your learning progress and continue with the next topic"
        ));
    }

    private Optional<ChiefInsight> analyzeTaskOverload() {
        int totalTasks = projectEngine.getTotalTaskCount();
        if (totalTasks >= TASK_OVERLOAD_THRESHOLD) {
            int openTasks = totalTasks - projectEngine.getAllProjects().stream()
                    .flatMap(p -> p.getTasks().stream().filter(t -> t.getStatus() == ProjectTask.Status.DONE))
                    .toArray().length;
            return Optional.of(new ChiefInsight(
                    ChiefInsight.Type.TASK_OVERLOAD,
                    ChiefInsight.Severity.MEDIUM,
                    60,
                    totalTasks + " tasks across all projects (" + openTasks + " open)",
                    "Focus on completing 3 high-priority tasks before starting new ones"
            ));
        }
        return Optional.empty();
    }

    private Optional<ChiefInsight> analyzePositiveProgress() {
        boolean hasProgress = projectEngine.getAllProjects().stream()
                .anyMatch(p -> p.getProgressPercentage() > 0);
        if (!hasProgress && !insights.isEmpty()) {
            return Optional.empty();
        }
        return Optional.empty(); // handled in project analysis
    }

    // ==================== RECOMMENDATION ====================

    private ChiefInsight generateRecommendation(List<ChiefInsight> newInsights) {
        if (newInsights.isEmpty()) {
            return new ChiefInsight(
                    ChiefInsight.Type.POSITIVE_PROGRESS,
                    ChiefInsight.Severity.LOW,
                    5,
                    "Everything looks on track. No significant issues detected.",
                    "Keep up the good work!"
            );
        }

        // Sort by priority score descending
        newInsights.sort((a, b) -> Integer.compare(b.getPriorityScore(), a.getPriorityScore()));
        return newInsights.get(0);
    }

    /**
     * Get the single highest-priority recommendation.
     */
    public ChiefInsight getRecommendation() {
        if (currentRecommendation == null) {
            analyze();
        }
        return currentRecommendation;
    }

    /**
     * Get all unresolved insights.
     */
    public List<ChiefInsight> getUnresolvedInsights() {
        return insights.stream()
                .filter(i -> !i.isResolved())
                .sorted((a, b) -> Integer.compare(b.getPriorityScore(), a.getPriorityScore()))
                .collect(Collectors.toList());
    }

    /**
     * Get all insights.
     */
    public List<ChiefInsight> getAllInsights() {
        return new ArrayList<>(insights);
    }

    /**
     * Get high-severity risks.
     */
    public List<ChiefInsight> getRisks() {
        return insights.stream()
                .filter(i -> !i.isResolved() && i.getSeverity() == ChiefInsight.Severity.HIGH)
                .sorted((a, b) -> Integer.compare(b.getPriorityScore(), a.getPriorityScore()))
                .collect(Collectors.toList());
    }

    /**
     * Mark an insight as resolved.
     */
    public boolean resolveInsight(String insightId) {
        Optional<ChiefInsight> insight = insights.stream()
                .filter(i -> i.getId().equals(insightId))
                .findFirst();
        if (insight.isPresent()) {
            insight.get().setResolved(true);
            save();
            analyze();
            return true;
        }
        return false;
    }

    /**
     * Get insights for prompt injection (max 3, highest priority).
     */
    public List<String> getContextInsights() {
        List<ChiefInsight> top = getUnresolvedInsights().stream()
                .limit(PROMPT_MAX_INSIGHTS)
                .collect(Collectors.toList());

        return top.stream()
                .map(i -> i.getType().name().replace("_", " ") + ": " + i.getMessage())
                .collect(Collectors.toList());
    }

    /**
     * Get full summary for dashboard.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        List<ChiefInsight> unresolved = getUnresolvedInsights();
        if (unresolved.isEmpty()) {
            sb.append("All systems nominal. No issues detected.\n");
        } else {
            sb.append(unresolved.size()).append(" insight(s) available.\n");
            ChiefInsight top = getRecommendation();
            if (top != null) {
                sb.append("Top priority: ").append(top.getMessage()).append("\n");
                sb.append("Recommendation: ").append(top.getRecommendation()).append("\n");
            }
        }
        return sb.toString();
    }

    public int getInsightCount() { return insights.size(); }
    public int getUnresolvedCount() { return (int) insights.stream().filter(i -> !i.isResolved()).count(); }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("insights", new ArrayList<>(insights));
            data.put("currentRecommendation", currentRecommendation);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(INSIGHTS_FILE), data);
        } catch (IOException e) {
            System.err.println("[ChiefOfStaff] Failed to save: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        try {
            File file = new File(INSIGHTS_FILE);
            if (!file.exists()) return;

            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            if (data.containsKey("insights")) {
                List<ChiefInsight> loaded = mapper.convertValue(data.get("insights"),
                        new TypeReference<List<ChiefInsight>>() {});
                insights.addAll(loaded);
            }

            if (data.containsKey("currentRecommendation")) {
                currentRecommendation = mapper.convertValue(data.get("currentRecommendation"), ChiefInsight.class);
            }

            System.out.println("[ChiefOfStaff] Loaded " + insights.size() + " insights");
        } catch (IOException e) {
            System.err.println("[ChiefOfStaff] Failed to load: " + e.getMessage());
        }
    }
}