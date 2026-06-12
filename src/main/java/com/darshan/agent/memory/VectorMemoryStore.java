package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class VectorMemoryStore {

    private final List<MemoryVector> vectors = new ArrayList<>();

    // STORE
    public void store(String text, double[] embedding) {
        vectors.add(new MemoryVector(text, embedding));
    }

    public List<MemoryVector> all() {
        return vectors;
    }


    // SEARCH
    public List<String> search(double[] query, int topK) {

        return vectors.stream()
                .sorted((a, b) -> Double.compare(
                        cosineSimilarity(b.getEmbedding(), query),
                        cosineSimilarity(a.getEmbedding(), query)
                ))
                .limit(topK)
                .map(MemoryVector::getText)
                .collect(Collectors.toList());
    }

    // COSINE SIMILARITY
    private double cosineSimilarity(double[] a, double[] b) {

        if (a.length != b.length) return 0;

        double dot = 0;
        double normA = 0;
        double normB = 0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB) + 1e-9);
    }
}

