package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemoryRecallService {

    private final VectorMemoryStore store;
    private final MemoryEmbedder embedder;

    public MemoryRecallService(VectorMemoryStore store,
                               MemoryEmbedder embedder) {
        this.store = store;
        this.embedder = embedder;
    }

    public String recall(String userInput) {

        // 1️⃣ embed query
        double[] queryVector =
                embedder.embed(userInput);

        // 2️⃣ search similar memories
        List<String> memories =
                store.search(queryVector, 5);

        if (memories.isEmpty())
            return "";

        // 3️⃣ build context block
        StringBuilder context = new StringBuilder();

        context.append("\nRelevant Memories:\n");

        for (String m : memories) {
            context.append("- ")
                    .append(m)
                    .append("\n");
        }

        return context.toString();
    }
}
