package com.darshan.agent.cognition;

import com.darshan.agent.memory.semantic.ConceptGraphEngine;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ConceptExtractionEngine {

    private final SemanticMemoryEngine semanticMemory;
    private final ConceptGraphEngine graph;



    public ConceptExtractionEngine(
            SemanticMemoryEngine semanticMemory,
            ConceptGraphEngine graph) {

        this.semanticMemory = semanticMemory;
        this.graph = graph;
    }
    /**
     * Extract important concepts from interaction.
     */
    public void extract(String userInput, String agentResponse) {

        String combined =
                (userInput + " " + agentResponse).toLowerCase();

        List<String> words =
                Arrays.stream(combined.split("\\W+"))
                        .toList();

        for (String word : words) {

            if (isConcept(word)) {
                semanticMemory.learnConcept(word);
            }
        }

        List<String> concepts =
                words.stream()
                        .filter(this::isConcept)
                        .distinct()
                        .toList();

        for (String c : concepts) {
            semanticMemory.learnConcept(c);
        }

        for (int i = 0; i < concepts.size() - 1; i++) {

            String a = concepts.get(i);
            String b = concepts.get(i + 1);

            graph.connect(a, "related_to", b);
        }
    }

    private boolean isConcept(String word) {

        // simple cognitive filter
        return word.length() > 3
                && !STOP_WORDS.contains(word);
    }

    private static final List<String> STOP_WORDS = List.of(
            "this","that","with","have","from","your",
            "what","when","where","will","would",
            "there","their","about","which","them",
            "been","into","just","like"
    );
}