package com.darshan.agent.graph;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KnowledgeEntity {
    private String id;
    private EntityType type;
    private String name;
    private String description;
    private Map<String, String> properties;
    private Instant createdAt;
    private Instant updatedAt;

    public KnowledgeEntity() {
        this.id = UUID.randomUUID().toString();
        this.properties = new HashMap<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public KnowledgeEntity(EntityType type, String name) {
        this();
        this.type = type;
        this.name = name;
    }

    public KnowledgeEntity(EntityType type, String name, String description) {
        this(type, name);
        this.description = description;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public EntityType getType() { return type; }
    public void setType(EntityType type) { this.type = type; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, String> getProperties() { return properties; }
    public void setProperties(Map<String, String> properties) { this.properties = properties; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public String getProperty(String key) {
        return properties.get(key);
    }

    public void setProperty(String key, String value) {
        properties.put(key, value);
        touch();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        KnowledgeEntity that = (KnowledgeEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}