package com.darshan.agent.autonomy;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class GoalManager {

    private static final String GOALS_FILE = "goals.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private AgentGoal currentGoal;

    @PostConstruct
    public void init() {
        load();
    }

    public void setGoal(AgentGoal goal) {
        this.currentGoal = goal;
        save();
    }

    public boolean hasGoal() {
        return currentGoal != null && !currentGoal.isCompleted();
    }

    public AgentGoal getGoal() {
        return currentGoal;
    }

    public void clearGoal() {
        this.currentGoal = null;
        save();
    }

    public void createGoal(String description) {
        String oldGoal = currentGoal != null ? currentGoal.getDescription() : "null";
        System.out.println("[GOAL_CREATE] old=" + oldGoal);
        System.out.println("[GOAL] createGoal() called with: " + description);
        if (currentGoal != null && !currentGoal.isCompleted()) {
            System.out.println("[GOAL] BLOCKED - already working on: " + currentGoal.getDescription());
            return; // already working on something
        }
        currentGoal = new AgentGoal(description);
        System.out.println("[GOAL_CREATE] new=" + currentGoal.getDescription());
        System.out.println("[GOAL] Goal after create: " + currentGoal.getDescription());
        save();
    }

    /**
     * Save goal state to goals.json.
     */
    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("currentGoal", currentGoal);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(GOALS_FILE), data);
        } catch (IOException e) {
            System.err.println("Failed to save goals: " + e.getMessage());
        }
    }

    /**
     * Load goal state from goals.json.
     */
    public synchronized void load() {
        try {
            File file = new File(GOALS_FILE);
            if (!file.exists()) {
                System.out.println("[GOAL] No goals.json file found");
                return;
            }
            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});
            if (data.containsKey("currentGoal")) {
                AgentGoal loaded = mapper.convertValue(data.get("currentGoal"), AgentGoal.class);
                if (loaded != null && !loaded.isCompleted()) {
                    this.currentGoal = loaded;
                    System.out.println("[GOAL_LOAD] restored goal=" + loaded.getDescription());
                    System.out.println("[GOAL] Goal restored from file: " + loaded.getDescription()
                            + " (" + loaded.getSubGoals().stream().filter(s -> !s.isCompleted()).count()
                            + " pending steps)");
                } else {
                    System.out.println("[GOAL] Loaded goal is null or completed");
                }
            } else {
                System.out.println("[GOAL] No currentGoal in file");
            }
        } catch (IOException e) {
            System.err.println("Failed to load goals: " + e.getMessage());
        }
    }
}