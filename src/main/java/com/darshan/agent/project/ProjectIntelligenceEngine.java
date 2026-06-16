package com.darshan.agent.project;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ProjectIntelligenceEngine {

    private static final String PROJECTS_FILE = "projects.json";
    private static final int MAX_CONTEXT_FACTS = 5;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, Project> projects = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        load();
    }

    // ==================== PROJECT OPERATIONS ====================

    public Project createProject(String name) {
        return createProject(name, null);
    }

    public Project createProject(String name, String description) {
        Optional<Project> existing = findProjectByName(name);
        if (existing.isPresent()) {
            return existing.get();
        }
        Project project = new Project(name, description);
        projects.put(project.getId(), project);
        save();
        return project;
    }

    public Optional<Project> getProject(String id) {
        return Optional.ofNullable(projects.get(id));
    }

    public Optional<Project> findProjectByName(String name) {
        return projects.values().stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<Project> getAllProjects() {
        return new ArrayList<>(projects.values());
    }

    public List<Project> getActiveProjects() {
        return projects.values().stream()
                .filter(p -> p.getStatus() != ProjectStatus.COMPLETED
                        && p.getStatus() != ProjectStatus.CANCELLED)
                .sorted(Comparator.comparing(Project::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public boolean removeProject(String id) {
        return projects.remove(id) != null;
    }

    // ==================== TASK OPERATIONS ====================

    public ProjectTask addTask(String projectId, String title, ProjectTask.Priority priority) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return null;

        ProjectTask task = new ProjectTask(title);
        task.setPriority(priority);
        project.get().getTasks().add(task);
        project.get().touch();
        save();
        return task;
    }

    public ProjectTask addTask(String projectId, String title) {
        return addTask(projectId, title, ProjectTask.Priority.MEDIUM);
    }

    public boolean completeTask(String projectId, String taskTitle) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return false;

        Optional<ProjectTask> task = project.get().getTasks().stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(taskTitle))
                .findFirst();

        if (task.isPresent() && task.get().getStatus() != ProjectTask.Status.DONE) {
            task.get().complete();
            project.get().touch();
            save();
            return true;
        }
        return false;
    }

    public boolean startTask(String projectId, String taskTitle) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return false;

        Optional<ProjectTask> task = project.get().getTasks().stream()
                .filter(t -> t.getTitle().equalsIgnoreCase(taskTitle))
                .findFirst();

        if (task.isPresent() && task.get().getStatus() == ProjectTask.Status.TODO) {
            task.get().setStatus(ProjectTask.Status.IN_PROGRESS);
            project.get().touch();
            save();
            return true;
        }
        return false;
    }

    // ==================== MILESTONE OPERATIONS ====================

    public ProjectMilestone addMilestone(String projectId, String title) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return null;

        ProjectMilestone milestone = new ProjectMilestone(title);
        project.get().getMilestones().add(milestone);
        project.get().touch();
        save();
        return milestone;
    }

    public boolean completeMilestone(String projectId, String milestoneTitle) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return false;

        Optional<ProjectMilestone> milestone = project.get().getMilestones().stream()
                .filter(m -> m.getTitle().equalsIgnoreCase(milestoneTitle))
                .findFirst();

        if (milestone.isPresent() && !milestone.get().isCompleted()) {
            milestone.get().complete();
            project.get().touch();
            save();
            return true;
        }
        return false;
    }

    // ==================== DECISION OPERATIONS ====================

    public ProjectDecision addDecision(String projectId, String decision, String reason) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return null;

        ProjectDecision d = new ProjectDecision(decision, reason);
        project.get().getDecisions().add(d);
        project.get().touch();
        save();
        return d;
    }

    // ==================== RISK OPERATIONS ====================

    public ProjectRisk addRisk(String projectId, String title, ProjectRisk.Severity severity) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return null;

        ProjectRisk risk = new ProjectRisk(title, severity);
        project.get().getRisks().add(risk);
        project.get().touch();
        save();
        return risk;
    }

    public boolean resolveRisk(String projectId, String riskTitle) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return false;

        Optional<ProjectRisk> risk = project.get().getRisks().stream()
                .filter(r -> r.getTitle().equalsIgnoreCase(riskTitle))
                .findFirst();

        if (risk.isPresent() && !risk.get().isResolved()) {
            risk.get().resolve();
            project.get().touch();
            save();
            return true;
        }
        return false;
    }

    // ==================== QUERY OPERATIONS ====================

    public List<ProjectTask> getPendingTasks(String projectId) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return List.of();
        return project.get().getTasks().stream()
                .filter(t -> t.getStatus() != ProjectTask.Status.DONE)
                .collect(Collectors.toList());
    }

    public List<ProjectRisk> getUnresolvedRisks(String projectId) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return List.of();
        return project.get().getRisks().stream()
                .filter(r -> !r.isResolved())
                .collect(Collectors.toList());
    }

    public List<ProjectDecision> getDecisions(String projectId) {
        Optional<Project> project = getProject(projectId);
        if (project.isEmpty()) return List.of();
        return new ArrayList<>(project.get().getDecisions());
    }

    /**
     * Get project status summary for dashboard.
     */
    public String getStatusSummary() {
        List<Project> active = getActiveProjects();
        if (active.isEmpty()) return "No active projects.";

        StringBuilder sb = new StringBuilder();
        for (Project p : active) {
            sb.append(p.getSummary()).append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * Get context facts for prompt injection (max 5 facts).
     */
    public List<String> getContextFacts(String input) {
        List<String> facts = new ArrayList<>();
        String lower = input.toLowerCase();

        // Find projects mentioned in input
        for (Project project : projects.values()) {
            if (lower.contains(project.getName().toLowerCase())) {
                facts.add("Project: " + project.getSummary());

                // Current tasks
                List<ProjectTask> inProgress = project.getTasks().stream()
                        .filter(t -> t.getStatus() == ProjectTask.Status.IN_PROGRESS)
                        .limit(2).collect(Collectors.toList());
                for (ProjectTask t : inProgress) {
                    facts.add("Active task: " + t.getTitle());
                }

                // Unresolved risks
                List<ProjectRisk> risks = getUnresolvedRisks(project.getId());
                for (ProjectRisk r : risks) {
                    facts.add("Risk: " + r.getTitle() + " (" + r.getSeverity() + ")");
                }
            }
        }

        // Add active projects as context if no specific mention
        if (facts.isEmpty()) {
            List<Project> active = getActiveProjects().stream().limit(2).collect(Collectors.toList());
            for (Project p : active) {
                facts.add("Active project: " + p.getSummary());
            }
        }

        return facts.stream().limit(MAX_CONTEXT_FACTS).collect(Collectors.toList());
    }

    // ==================== EXTRACTION ====================

    /**
     * Extract project intelligence from user input.
     */
    public void extractFromInput(String input) {
        String lower = input.toLowerCase();

        // Project creation: "I am building X", "my project X"
        if (lower.contains("i am building") || lower.contains("i'm building")
                || lower.contains("my project") || lower.contains("working on")) {
            String projectName = extractProjectName(input);
            if (!projectName.isEmpty()) {
                createProject(projectName, "Auto-created from user input");
            }
        }

        // Task completion: "I completed X", "finished X", "done with X"
        if (lower.contains("i completed") || lower.contains("finished ")
                || lower.contains("done with") || lower.contains("i finished")) {
            String taskName = extractAfter(input, new String[]{"completed ", "finished ", "done with "});
            if (!taskName.isEmpty()) {
                // Try to find which project this belongs to
                Optional<Project> project = findProjectForTask(taskName);
                if (project.isPresent()) {
                    completeTask(project.get().getId(), taskName);
                }
            }
        }

        // Task in progress: "I am working on X", "working on X"
        if (lower.contains("i am working on") || lower.contains("i'm working on")) {
            String taskName = extractAfter(input, new String[]{"working on "});
            if (!taskName.isEmpty()) {
                Optional<Project> project = findProjectForTask(taskName);
                if (project.isPresent()) {
                    startTask(project.get().getId(), taskName);
                } else {
                    // Create as new task in first active project
                    List<Project> active = getActiveProjects();
                    if (!active.isEmpty()) {
                        addTask(active.get(0).getId(), taskName, ProjectTask.Priority.HIGH);
                    }
                }
            }
        }

        // Decision: "I decided to X", "decision: X"
        if (lower.contains("i decided") || lower.contains("decision:")) {
            String decision = extractAfter(input, new String[]{"decided to ", "decided on ", "decision: "});
            if (!decision.isEmpty()) {
                List<Project> active = getActiveProjects();
                if (!active.isEmpty()) {
                    addDecision(active.get(0).getId(), decision, "User decision");
                }
            }
        }

        // Risk: "X is blocking", "risk: X", "problem with X"
        if (lower.contains(" is blocking") || lower.contains("risk:")
                || lower.contains("problem with") || lower.contains("blocked by")) {
            String risk = extractAfter(input, new String[]{" is blocking", "risk: ", "problem with ", "blocked by "});
            if (!risk.isEmpty()) {
                List<Project> active = getActiveProjects();
                if (!active.isEmpty()) {
                    addRisk(active.get(0).getId(), risk, ProjectRisk.Severity.MEDIUM);
                }
            }
        }
    }

    private String extractProjectName(String input) {
        String lower = input.toLowerCase();
        String[] prefixes = {"building ", "my project ", "working on "};
        for (String prefix : prefixes) {
            int idx = lower.indexOf(prefix);
            if (idx >= 0) {
                String name = input.substring(idx + prefix.length()).trim();
                name = name.replaceAll("[.!?]+$", "").trim();
                if (name.length() > 100) name = name.substring(0, 100);
                if (!name.isEmpty()) {
                    return name.substring(0, 1).toUpperCase() + name.substring(1);
                }
            }
        }
        return "";
    }

    private String extractAfter(String input, String[] prefixes) {
        String lower = input.toLowerCase();
        for (String prefix : prefixes) {
            int idx = lower.indexOf(prefix);
            if (idx >= 0) {
                String result = input.substring(idx + prefix.length()).trim();
                result = result.replaceAll("[.!?]+$", "").trim();
                if (result.length() > 100) result = result.substring(0, 100);
                if (!result.isEmpty()) {
                    return result.substring(0, 1).toUpperCase() + result.substring(1);
                }
            }
        }
        return "";
    }

    private Optional<Project> findProjectForTask(String taskName) {
        String lower = taskName.toLowerCase();
        return projects.values().stream()
                .filter(p -> p.getTasks().stream()
                        .anyMatch(t -> t.getTitle().toLowerCase().contains(lower)))
                .findFirst();
    }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("projects", new ArrayList<>(projects.values()));
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(PROJECTS_FILE), data);
        } catch (IOException e) {
            System.err.println("[ProjectIntelligence] Failed to save: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        try {
            File file = new File(PROJECTS_FILE);
            if (!file.exists()) return;

            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            if (data.containsKey("projects")) {
                List<Project> projectList = mapper.convertValue(data.get("projects"),
                        new TypeReference<List<Project>>() {});
                for (Project p : projectList) {
                    projects.put(p.getId(), p);
                }
            }

            System.out.println("[ProjectIntelligence] Loaded " + projects.size() + " projects");
        } catch (IOException e) {
            System.err.println("[ProjectIntelligence] Failed to load: " + e.getMessage());
        }
    }

    // ==================== STATS ====================

    public int getProjectCount() { return projects.size(); }

    public int getTotalTaskCount() {
        return projects.values().stream()
                .mapToInt(p -> p.getTasks().size())
                .sum();
    }
}