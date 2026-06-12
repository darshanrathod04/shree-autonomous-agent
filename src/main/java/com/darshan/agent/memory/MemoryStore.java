package com.darshan.agent.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
public class MemoryStore {

    private static final String FILE = "memory.json";

    private final ObjectMapper mapper = new ObjectMapper();

    public MemoryFile load() {
        try {
            File file = new File(FILE);

            if (!file.exists()) {
                MemoryFile memory = new MemoryFile();
                save(memory);
                return memory;
            }

            return mapper.readValue(file, MemoryFile.class);

        } catch (Exception e) {
            e.printStackTrace();
            return new MemoryFile();
        }
    }

    public void save(MemoryFile memory) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE), memory);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addConversation(ConversationEntry entry) {

        MemoryFile memory = load();
        memory.getHistory().add(entry);
        save(memory);
    }
}
