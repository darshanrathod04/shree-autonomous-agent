package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MemoryRecallEngine {

    private final VectorMemoryStore store;
    private final MemoryEmbedder embedder;

    public MemoryRecallEngine(
            VectorMemoryStore store,
            MemoryEmbedder embedder) {

        this.store = store;
        this.embedder = embedder;
    }

    public List<String> recall(String input) {

        double[] query = embedder.embed(input);

        return store.all().stream()
                .sorted(Comparator.comparingDouble(
                        m -> -similarity(query, m.getEmbedding())))
                .limit(3)
                .map(MemoryVector::getText)
                .collect(Collectors.toList());
    }

    private double similarity(double[] a, double[] b) {

        double sum = 0;

        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }

        return sum;
    }
}
