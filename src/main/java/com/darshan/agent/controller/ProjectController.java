 package com.darshan.agent.controller;

import com.darshan.agent.project.Project;
import com.darshan.agent.project.ProjectIntelligenceEngine;
import com.darshan.agent.project.ProjectRisk;
import com.darshan.agent.project.ProjectTask;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent/projects")
public class ProjectController {

    private final ProjectIntelligenceEngine projectEngine;

    public ProjectController(ProjectIntelligenceEngine projectEngine) {
        this.projectEngine = projectEngine;
    }

    @GetMapping
    public Map<String, Object> getProjects() {
        Map<String, Object> result = new HashMap<>();
        List<Project> projects = projectEngine.getAllProjects();
        result.put("projects", projects.stream().map(p -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", p.getId());
            map.put("name", p.getName());
            map.put("description", p.getDescription());
            map.put("status", p.getStatus().name());
            map.put("progress", p.getProgressPercentage());
            map.put("taskCount", p.getTasks().size());
            map.put("doneTasks", p.getDoneTaskCount());
            map.put("riskCount", p.getRisks().size());
            map.put("unresolvedRisks", p.getUnresolvedRiskCount());
            return map;
        }).collect(Collectors.toList()));
        result.put("count", projects.size());
        return result;
    }

    @GetMapping("/status")
    public Map<String, Object> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", projectEngine.getStatusSummary());
        result.put("activeProjects", projectEngine.getActiveProjects().size());
        result.put("totalProjects", projectEngine.getProjectCount());
        return result;
    }

    @GetMapping("/tasks")
    public Map<String, Object> getTasks() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> allTasks = projectEngine.getAllProjects().stream()
                .flatMap(p -> p.getTasks().stream().map(t -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("projectName", p.getName());
                    map.put("title", t.getTitle());
                    map.put("status", t.getStatus().name());
                    map.put("priority", t.getPriority().name());
                    return map;
                }))
                .collect(Collectors.toList());
        result.put("tasks", allTasks);
        result.put("totalCount", allTasks.size());
        return result;
    }

    @GetMapping("/risks")
    public Map<String, Object> getRisks() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> unresolvedRisks = projectEngine.getAllProjects().stream()
                .flatMap(p -> projectEngine.getUnresolvedRisks(p.getId()).stream().map(r -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("projectName", p.getName());
                    map.put("title", r.getTitle());
                    map.put("severity", r.getSeverity().name());
                    return map;
                }))
                .collect(Collectors.toList());
        result.put("risks", unresolvedRisks);
        result.put("count", unresolvedRisks.size());
        return result;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("statusSummary", projectEngine.getStatusSummary());
        result.put("projectCount", projectEngine.getProjectCount());
        result.put("totalTasks", projectEngine.getTotalTaskCount());
        result.put("activeProjects", projectEngine.getActiveProjects().stream()
                .map(p -> p.getName() + " (" + (int) p.getProgressPercentage() + "%)")
                .collect(Collectors.toList()));
        return result;
    }
}