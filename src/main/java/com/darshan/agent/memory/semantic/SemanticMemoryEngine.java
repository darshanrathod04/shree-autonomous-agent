package com.darshan.agent.memory.semantic;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SemanticMemoryEngine {

    private final Map<String, SemanticConcept> knowledge =
            new HashMap<>();

    private final Map<String, Concept> concepts = new HashMap<>();

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
}