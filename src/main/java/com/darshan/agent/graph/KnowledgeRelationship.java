package com.darshan.agent.graph;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnowledgeRelationship {
    private String id;
    private String sourceId;
    private String targetId;
    private RelationshipType type;
    private Map<String, String> properties;
    private Instant createdAt;

    public KnowledgeRelationship() {
        this.id = UUID.randomUUID().toString();
        this.properties = new HashMap<>();
        this.createdAt = Instant.now();
    }

    public KnowledgeRelationship(String sourceId, RelationshipType type, String targetId) {
        this();
        this.sourceId = sourceId;
        this.type = type;
        this.targetId = targetId;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getSourceId() { return sourceId; }
    public void setSourceId(String sourceId) { this.sourceId = sourceId; }
    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }
    public RelationshipType getType() { return type; }
    public void setType(RelationshipType type) { this.type = type; }
    public Map<String, String> getProperties() { return properties; }
    public void setProperties(Map<String, String> properties) { this.properties = properties; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
    }
}