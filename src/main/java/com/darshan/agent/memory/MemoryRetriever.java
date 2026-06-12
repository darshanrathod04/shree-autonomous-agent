package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

@Component
public class MemoryRetriever {

    private final MemoryStore store;

    public MemoryRetriever(MemoryStore store) {
        this.store = store;
    }

    public String buildContext() {

        MemoryFile memory = store.load();

        StringBuilder sb = new StringBuilder();

        for (ConversationEntry e : memory.getHistory()) {

            sb.append("User: ")
                    .append(e.getUser())
                    .append("\n");

            sb.append("Shree: ")
                    .append(e.getAssistant())
                    .append("\n");
        }

        return sb.toString();
    }
}
