package com.darshan.agent.cognition.learning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
public class LearningStore {

    private static final String FILE = "learning.json";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<LearningRecord> load() {
        try {
            File file = new File(FILE);

            if (!file.exists()) {
                save(new ArrayList<>());
            }

            return mapper.readValue(
                    file,
                    mapper.getTypeFactory()
                            .constructCollectionType(
                                    List.class,
                                    LearningRecord.class));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void save(List<LearningRecord> data) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(FILE), data);
        } catch (Exception ignored) {}
    }

    public void add(LearningRecord record) {
        List<LearningRecord> list = load();
        list.add(record);
        save(list);
    }
}
