package com.darshan.agent.graph;

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
public class KnowledgeGraphEngine {

    private static final String GRAPH_FILE = "knowledge_graph.json";
    private static final int MAX_CONTEXT_FACTS = 10;

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final Map<String, KnowledgeEntity> entities = new ConcurrentHashMap<>();
    private final List<KnowledgeRelationship> relationships = Collections.synchronizedList(new ArrayList<>());

    @PostConstruct
    public void init() {
        load();
    }

    // ==================== ENTITY OPERATIONS ====================

    public KnowledgeEntity addEntity(EntityType type, String name) {
        return addEntity(type, name, null);
    }

    public KnowledgeEntity addEntity(EntityType type, String name, String description) {
        // Check if entity with same type and name already exists
        Optional<KnowledgeEntity> existing = entities.values().stream()
                .filter(e -> e.getType() == type && e.getName().equalsIgnoreCase(name))
                .findFirst();

        if (existing.isPresent()) {
            KnowledgeEntity entity = existing.get();
            if (description != null) {
                entity.setDescription(description);
            }
            entity.touch();
            save();
            return entity;
        }

        KnowledgeEntity entity = new KnowledgeEntity(type, name, description);
        entities.put(entity.getId(), entity);
        save();
        return entity;
    }

    public Optional<KnowledgeEntity> getEntity(String id) {
        return Optional.ofNullable(entities.get(id));
    }

    public Optional<KnowledgeEntity> findEntity(EntityType type, String name) {
        return entities.values().stream()
                .filter(e -> e.getType() == type && e.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public List<KnowledgeEntity> getEntitiesByType(EntityType type) {
        return entities.values().stream()
                .filter(e -> e.getType() == type)
                .sorted(Comparator.comparing(KnowledgeEntity::getUpdatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<KnowledgeEntity> getAllEntities() {
        return new ArrayList<>(entities.values());
    }

    public boolean removeEntity(String id) {
        KnowledgeEntity removed = entities.remove(id);
        if (removed != null) {
            relationships.removeIf(r -> r.getSourceId().equals(id) || r.getTargetId().equals(id));
            save();
            return true;
        }
        return false;
    }

    // ==================== RELATIONSHIP OPERATIONS ====================

    public KnowledgeRelationship addRelationship(String sourceId, RelationshipType type, String targetId) {
        // Check for duplicate relationship
        boolean exists = relationships.stream()
                .anyMatch(r -> r.getSourceId().equals(sourceId)
                        && r.getType() == type
                        && r.getTargetId().equals(targetId));

        if (exists) {
            return relationships.stream()
                    .filter(r -> r.getSourceId().equals(sourceId)
                            && r.getType() == type
                            && r.getTargetId().equals(targetId))
                    .findFirst().orElse(null);
        }

        KnowledgeRelationship rel = new KnowledgeRelationship(sourceId, type, targetId);
        relationships.add(rel);
        save();
        return rel;
    }

    public KnowledgeRelationship addRelationship(String sourceId, RelationshipType type, String targetId,
                                                  Map<String, String> properties) {
        KnowledgeRelationship rel = addRelationship(sourceId, type, targetId);
        if (rel != null && properties != null) {
            rel.getProperties().putAll(properties);
            save();
        }
        return rel;
    }

    public List<KnowledgeRelationship> getRelationshipsFrom(String sourceId) {
        return relationships.stream()
                .filter(r -> r.getSourceId().equals(sourceId))
                .collect(Collectors.toList());
    }

    public List<KnowledgeRelationship> getRelationshipsTo(String targetId) {
        return relationships.stream()
                .filter(r -> r.getTargetId().equals(targetId))
                .collect(Collectors.toList());
    }

    public List<KnowledgeRelationship> getRelationshipsByType(RelationshipType type) {
        return relationships.stream()
                .filter(r -> r.getType() == type)
                .collect(Collectors.toList());
    }

    public List<KnowledgeRelationship> getAllRelationships() {
        return new ArrayList<>(relationships);
    }

    public boolean removeRelationship(String relationshipId) {
        boolean removed = relationships.removeIf(r -> r.getId().equals(relationshipId));
        if (removed) {
            save();
        }
        return removed;
    }

    // ==================== QUERY OPERATIONS ====================

    public List<KnowledgeEntity> getRelatedEntities(String entityId, RelationshipType type, boolean outgoing) {
        List<KnowledgeEntity> result = new ArrayList<>();

        if (outgoing) {
            for (KnowledgeRelationship r : relationships) {
                if (r.getSourceId().equals(entityId) && r.getType() == type) {
                    entities.values().stream()
                            .filter(e -> e.getId().equals(r.getTargetId()))
                            .findFirst()
                            .ifPresent(result::add);
                }
            }
        } else {
            for (KnowledgeRelationship r : relationships) {
                if (r.getTargetId().equals(entityId) && r.getType() == type) {
                    entities.values().stream()
                            .filter(e -> e.getId().equals(r.getSourceId()))
                            .findFirst()
                            .ifPresent(result::add);
                }
            }
        }

        return result;
    }

    /**
     * Get user summary: what they own, work on, learn, etc.
     */
    public String getUserSummary(String userName) {
        Optional<KnowledgeEntity> user = findEntity(EntityType.USER, userName);
        if (user.isEmpty()) {
            return "No user profile in knowledge graph.";
        }

        String userId = user.get().getId();
        StringBuilder sb = new StringBuilder();

        // Projects
        List<KnowledgeEntity> projects = getRelatedEntities(userId, RelationshipType.WORKS_ON, true);
        if (!projects.isEmpty()) {
            sb.append("Projects: ").append(projects.stream()
                    .map(KnowledgeEntity::getName).collect(Collectors.joining(", "))).append("\n");
        }

        // Learning
        List<KnowledgeEntity> learning = getRelatedEntities(userId, RelationshipType.LEARNING, true);
        if (!learning.isEmpty()) {
            sb.append("Learning: ").append(learning.stream()
                    .map(KnowledgeEntity::getName).collect(Collectors.joining(", "))).append("\n");
        }

        // Goals
        List<KnowledgeEntity> goals = getRelatedEntities(userId, RelationshipType.INTERESTED_IN, true);
        if (!goals.isEmpty()) {
            sb.append("Goals: ").append(goals.stream()
                    .map(KnowledgeEntity::getName).collect(Collectors.joining(", "))).append("\n");
        }

        return sb.toString().trim();
    }

    /**
     * Get context facts for prompt injection (max 10 facts).
     */
    public List<String> getContextFacts(String input) {
        List<String> facts = new ArrayList<>();
        String lower = input.toLowerCase();

        // Find entities mentioned in input
        for (KnowledgeEntity entity : entities.values()) {
            if (lower.contains(entity.getName().toLowerCase())) {
                // Get relationships for this entity
                List<KnowledgeRelationship> rels = getRelationshipsFrom(entity.getId());
                for (KnowledgeRelationship rel : rels) {
                    Optional<KnowledgeEntity> target = getEntity(rel.getTargetId());
                    if (target.isPresent()) {
                        facts.add(entity.getName() + " " + rel.getType().name().toLowerCase()
                                .replace("_", " ") + " " + target.get().getName());
                    }
                }
                // Also get reverse relationships
                List<KnowledgeRelationship> incomingRels = getRelationshipsTo(entity.getId());
                for (KnowledgeRelationship rel : incomingRels) {
                    Optional<KnowledgeEntity> source = getEntity(rel.getSourceId());
                    if (source.isPresent()) {
                        facts.add(source.get().getName() + " " + rel.getType().name().toLowerCase()
                                .replace("_", " ") + " " + entity.getName());
                    }
                }
            }
        }

        // Add recent entities as context
        List<KnowledgeEntity> recentProjects = getEntitiesByType(EntityType.PROJECT).stream()
                .limit(3).collect(Collectors.toList());
        for (KnowledgeEntity p : recentProjects) {
            String fact = "Known project: " + p.getName();
            if (!facts.contains(fact)) facts.add(fact);
        }

        List<KnowledgeEntity> recentGoals = getEntitiesByType(EntityType.GOAL).stream()
                .limit(3).collect(Collectors.toList());
        for (KnowledgeEntity g : recentGoals) {
            String fact = "Known goal: " + g.getName();
            if (!facts.contains(fact)) facts.add(fact);
        }

        List<KnowledgeEntity> recentLearning = getEntitiesByType(EntityType.LEARNING_TOPIC).stream()
                .limit(3).collect(Collectors.toList());
        for (KnowledgeEntity l : recentLearning) {
            String fact = "Learning: " + l.getName();
            if (!facts.contains(fact)) facts.add(fact);
        }

        // Limit to MAX_CONTEXT_FACTS
        return facts.stream().limit(MAX_CONTEXT_FACTS).collect(Collectors.toList());
    }

    // ==================== EXTRACTION ====================

    /**
     * Extract knowledge graph facts from user input.
     * Simple pattern-based extraction (no LLM required).
     */
    public void extractFromInput(String input) {
        String lower = input.toLowerCase();

        // Ensure user entity exists
        KnowledgeEntity user = findEntity(EntityType.USER, "Darshan")
                .orElseGet(() -> addEntity(EntityType.USER, "Darshan", "Primary user"));

        // Learning patterns
        if (lower.contains("i am learning") || lower.contains("i'm learning")
                || lower.contains("learning ") || lower.contains("teach me ")) {
            String topic = extractTopic(input, new String[]{"learning ", "teach me ", "i am learning ", "i'm learning "});
            if (!topic.isEmpty()) {
                KnowledgeEntity topicEntity = addEntity(EntityType.LEARNING_TOPIC, topic);
                addRelationship(user.getId(), RelationshipType.LEARNING, topicEntity.getId());
            }
        }

        // Project patterns
        if (lower.contains("i am building") || lower.contains("i'm building")
                || lower.contains("working on") || lower.contains("my project")) {
            String project = extractTopic(input, new String[]{"building ", "working on ", "my project ", "i am building ", "i'm building "});
            if (!project.isEmpty()) {
                KnowledgeEntity projectEntity = addEntity(EntityType.PROJECT, project);
                addRelationship(user.getId(), RelationshipType.WORKS_ON, projectEntity.getId());
            }
        }

        // Goal patterns
        if (lower.contains("my goal") || lower.contains("i want to") || lower.contains("i aim to")) {
            String goal = extractTopic(input, new String[]{"my goal is to ", "my goal is ", "i want to ", "i aim to "});
            if (!goal.isEmpty()) {
                KnowledgeEntity goalEntity = addEntity(EntityType.GOAL, goal);
                addRelationship(user.getId(), RelationshipType.INTERESTED_IN, goalEntity.getId());
            }
        }

        // Decision patterns
        if (lower.contains("i decided") || lower.contains("i've decided") || lower.contains("i have decided")) {
            String decision = extractTopic(input, new String[]{"decided to ", "decided on ", "i decided "});
            if (!decision.isEmpty()) {
                KnowledgeEntity decisionEntity = addEntity(EntityType.DECISION, decision);
                addRelationship(user.getId(), RelationshipType.DECIDED, decisionEntity.getId());
            }
        }

        // Skill patterns
        if (lower.contains("i know") || lower.contains("i am good at") || lower.contains("my skill")) {
            String skill = extractTopic(input, new String[]{"i know ", "i am good at ", "my skill is ", "skilled in "});
            if (!skill.isEmpty()) {
                KnowledgeEntity skillEntity = addEntity(EntityType.SKILL, skill);
                addRelationship(user.getId(), RelationshipType.INTERESTED_IN, skillEntity.getId());
            }
        }

        // Note patterns
        if (lower.contains("remember that") || lower.contains("note:") || lower.contains("important:")) {
            String note = extractTopic(input, new String[]{"remember that ", "note: ", "important: "});
            if (!note.isEmpty()) {
                addEntity(EntityType.NOTE, note);
            }
        }
    }

    private String extractTopic(String input, String[] prefixes) {
        String lower = input.toLowerCase();
        for (String prefix : prefixes) {
            int idx = lower.indexOf(prefix);
            if (idx >= 0) {
                String topic = input.substring(idx + prefix.length()).trim();
                // Clean up: remove trailing punctuation and limit length
                topic = topic.replaceAll("[.!?]+$", "").trim();
                if (topic.length() > 100) {
                    topic = topic.substring(0, 100);
                }
                if (!topic.isEmpty()) {
                    // Capitalize first letter
                    return topic.substring(0, 1).toUpperCase() + topic.substring(1);
                }
            }
        }
        return "";
    }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("entities", new ArrayList<>(entities.values()));
            data.put("relationships", new ArrayList<>(relationships));
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(GRAPH_FILE), data);
        } catch (IOException e) {
            System.err.println("[KnowledgeGraph] Failed to save: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        try {
            File file = new File(GRAPH_FILE);
            if (!file.exists()) {
                // Create default user entity
                addEntity(EntityType.USER, "Darshan", "Primary user");
                return;
            }

            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            if (data.containsKey("entities")) {
                List<KnowledgeEntity> entityList = mapper.convertValue(data.get("entities"),
                        new TypeReference<List<KnowledgeEntity>>() {});
                for (KnowledgeEntity e : entityList) {
                    entities.put(e.getId(), e);
                }
            }

            if (data.containsKey("relationships")) {
                List<KnowledgeRelationship> relList = mapper.convertValue(data.get("relationships"),
                        new TypeReference<List<KnowledgeRelationship>>() {});
                relationships.addAll(relList);
            }

            System.out.println("[KnowledgeGraph] Loaded " + entities.size() + " entities, "
                    + relationships.size() + " relationships");
        } catch (IOException e) {
            System.err.println("[KnowledgeGraph] Failed to load: " + e.getMessage());
            // Create default user entity on load failure
            addEntity(EntityType.USER, "Darshan", "Primary user");
        }
    }

    // ==================== STATS ====================

    public int getEntityCount() { return entities.size(); }
    public int getRelationshipCount() { return relationships.size(); }
}