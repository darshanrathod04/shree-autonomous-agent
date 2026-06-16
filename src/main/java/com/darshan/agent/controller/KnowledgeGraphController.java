package com.darshan.agent.controller;

import com.darshan.agent.graph.KnowledgeGraphEngine;
import com.darshan.agent.graph.KnowledgeEntity;
import com.darshan.agent.graph.KnowledgeRelationship;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/graph")
public class KnowledgeGraphController {

    private final KnowledgeGraphEngine graphEngine;

    public KnowledgeGraphController(KnowledgeGraphEngine graphEngine) {
        this.graphEngine = graphEngine;
    }

    @GetMapping("/entities")
    public Map<String, Object> getEntities() {
        Map<String, Object> result = new HashMap<>();
        List<KnowledgeEntity> entities = graphEngine.getAllEntities();
        result.put("entities", entities);
        result.put("count", entities.size());
        return result;
    }

    @GetMapping("/relationships")
    public Map<String, Object> getRelationships() {
        Map<String, Object> result = new HashMap<>();
        List<KnowledgeRelationship> relationships = graphEngine.getAllRelationships();
        result.put("relationships", relationships);
        result.put("count", relationships.size());
        return result;
    }

    @GetMapping("/user-summary")
    public Map<String, Object> getUserSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", graphEngine.getUserSummary("Darshan"));
        result.put("entityCount", graphEngine.getEntityCount());
        result.put("relationshipCount", graphEngine.getRelationshipCount());
        return result;
    }
}