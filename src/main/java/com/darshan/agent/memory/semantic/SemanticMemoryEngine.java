package com.darshan.agent.memory.semantic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Component
public class SemanticMemoryEngine {

    private static final String SEMANTIC_FILE = "semantic_memory.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    
    // Concept's lastSeen uses LocalDateTime which needs JSR310 module.
    // We handle it by not serializing lastSeen directory - we use Map serialization instead.

    private final Map<String, SemanticConcept> knowledge = new HashMap<>();
    private final Map<String, Concept> concepts = new HashMap<>();

    @PostConstruct
    public void init() {
        load();
    }

    public boolean knows(String concept) {
        if (concept == null) return false;
        return concepts.containsKey(concept.toLowerCase());
    }

    public void learnConcept(String word) {
        word = word.toLowerCase();
        if (word.length() < 3) return;

        Concept concept = concepts.get(word);
        if (concept == null) {
            concepts.put(word, new Concept(word));
        } else {
            concept.reinforce();
        }
        save();
    }

    public List<Concept> topConcepts(int limit) {
        return concepts.values().stream()
                .sorted(Comparator.comparingInt(Concept::getFrequency).reversed())
                .limit(limit)
                .toList();
    }

    public void learn(String concept, String meaning) {
        knowledge.compute(concept.toLowerCase(),
                (k, existing) -> {
                    if (existing == null)
                        return new SemanticConcept(concept, meaning);
                    existing.reinforce();
                    return existing;
                });
        save();
    }

    public String recall(String keyword) {
        return knowledge.values().stream()
                .filter(c ->
                        keyword.toLowerCase()
                                .contains(c.getConcept().toLowerCase()))
                .map(SemanticConcept::getMeaning)
                .findFirst()
                .orElse("");
    }

    public int size() {
        return knowledge.size();
    }

    public Map<String, SemanticConcept> getKnowledge() {
        return knowledge;
    }

    public Map<String, Concept> getConcepts() {
        return concepts;
    }

    /**
     * Save semantic memory to semantic_memory.json.
     * Uses simple Map serialization to avoid JSR310 dependency for LocalDateTime.
     */
    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            
            // Save knowledge as simple Maps
            Map<String, Map<String, Object>> simpleKnowledge = new HashMap<>();
            for (Map.Entry<String, SemanticConcept> entry : knowledge.entrySet()) {
                Map<String, Object> sc = new HashMap<>();
                sc.put("concept", entry.getValue().getConcept());
                sc.put("meaning", entry.getValue().getMeaning());
                sc.put("frequency", entry.getValue().getReinforcement());
                simpleKnowledge.put(entry.getKey(), sc);
            }
            data.put("knowledge", simpleKnowledge);
            
            // Save concepts as simple Maps (avoiding LocalDateTime)
            Map<String, Map<String, Object>> simpleConcepts = new HashMap<>();
            for (Map.Entry<String, Concept> entry : concepts.entrySet()) {
                Map<String, Object> c = new HashMap<>();
                c.put("word", entry.getValue().getName());
                c.put("frequency", entry.getValue().getFrequency());
                simpleConcepts.put(entry.getKey(), c);
            }
            data.put("concepts", simpleConcepts);
            
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(SEMANTIC_FILE), data);
        } catch (IOException e) {
            System.err.println("Failed to save semantic memory: " + e.getMessage());
        }
    }

    /**
     * Load semantic memory from semantic_memory.json.
     */
    public synchronized void load() {
        try {
            File file = new File(SEMANTIC_FILE);
            if (!file.exists()) {
                return;
            }
            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});
            if (data.containsKey("knowledge")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> loadedKnowledgeRaw =
                        (Map<String, Object>) data.get("knowledge");
                knowledge.clear();
                if (loadedKnowledgeRaw != null) {
                    // Convert LinkedHashMap entries back to SemanticConcept
                    for (Map.Entry<String, Object> entry : loadedKnowledgeRaw.entrySet()) {
                        Object val = entry.getValue();
                        if (val instanceof SemanticConcept) {
                            knowledge.put(entry.getKey(), (SemanticConcept) val);
                        } else if (val instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> conceptData = (Map<String, Object>) val;
                            String concept = (String) conceptData.getOrDefault("concept", entry.getKey());
                            String meaning = (String) conceptData.getOrDefault("meaning", "");
                            int frequency = conceptData.containsKey("frequency")
                                    ? ((Number) conceptData.get("frequency")).intValue() : 1;
                            SemanticConcept sc = new SemanticConcept(concept, meaning);
                            for (int i = 1; i < frequency; i++) sc.reinforce();
                            knowledge.put(entry.getKey(), sc);
                        }
                    }
                }
            }
            if (data.containsKey("concepts")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> loadedConcepts =
                        (Map<String, Object>) data.get("concepts");
                concepts.clear();
                if (loadedConcepts != null) {
                    for (Map.Entry<String, Object> entry : loadedConcepts.entrySet()) {
                        Object val = entry.getValue();
                        if (val instanceof Concept) {
                            concepts.put(entry.getKey(), (Concept) val);
                        } else if (val instanceof Map) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> conceptData = (Map<String, Object>) val;
                            String word = (String) conceptData.getOrDefault("word", entry.getKey());
                            int frequency = conceptData.containsKey("frequency")
                                    ? ((Number) conceptData.get("frequency")).intValue() : 1;
                            Concept c = new Concept(word, frequency);
                            concepts.put(entry.getKey(), c);
                        }
                    }
                }
            }
            System.out.println("📚 Semantic memory loaded: "
                    + knowledge.size() + " knowledge entries, "
                    + concepts.size() + " concepts");
        } catch (IOException e) {
            System.err.println("Failed to load semantic memory: " + e.getMessage());
        }
    }
}