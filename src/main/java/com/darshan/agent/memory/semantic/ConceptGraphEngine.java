package com.darshan.agent.memory.semantic;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ConceptGraphEngine {

    private final Map<String, List<ConceptRelation>> graph =
            new HashMap<>();

    /**
     * Create or reinforce connection
     */
    public void connect(String source,
                        String relation,
                        String target) {

        // ✅ NEVER mutate lambda variables
        final String normalizedSource = source.toLowerCase();
        final String normalizedTarget = target.toLowerCase();
        final String normalizedRelation = relation.toLowerCase();

        graph.putIfAbsent(normalizedSource, new ArrayList<>());

        List<ConceptRelation> edges = graph.get(normalizedSource);

        Optional<ConceptRelation> existing =
                edges.stream()
                        .filter(r ->
                                r.getRelation().equals(normalizedRelation)
                                        && r.getTarget().equals(normalizedTarget))
                        .findFirst();

        if (existing.isPresent()) {
            existing.get().reinforce();
        } else {
            edges.add(
                    new ConceptRelation(
                            normalizedSource,
                            normalizedRelation,
                            normalizedTarget
                    )
            );
        }
    }

    /**
     * Get associations
     */
    public List<ConceptRelation> relatedTo(String concept) {

        if (concept == null) return List.of();

        return graph.getOrDefault(
                concept.toLowerCase(),
                List.of()
        );
    }

    /**
     * Cognitive recall summary
     */
    public String summarize(String concept) {

        List<ConceptRelation> relations = relatedTo(concept);

        if (relations.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("Known relations:\n");

        for (ConceptRelation r : relations) {
            sb.append("- ")
                    .append(r.getSource())
                    .append(" ")
                    .append(r.getRelation())
                    .append(" ")
                    .append(r.getTarget())
                    .append("\n");
        }

        return sb.toString();
    }
}