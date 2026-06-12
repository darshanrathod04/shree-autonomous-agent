package com.darshan.agent.llm;

import com.darshan.agent.memory.MemoryRecallEngine;
import com.darshan.agent.memory.MemoryRecallService;
import com.darshan.agent.memory.MemoryRetriever;
import com.darshan.agent.personality.AgentPersonality;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private static final String URL =
            "http://localhost:11434/api/generate";

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(120, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

    private final ObjectMapper mapper = new ObjectMapper();
    private final AgentPersonality personality;
    private final MemoryRetriever memory;
    private final MemoryRecallEngine recall;
    private final MemoryRecallService recallService;




    public OllamaClient(AgentPersonality personality, MemoryRetriever memory, MemoryRecallEngine recall, MemoryRecallService recallService) {
        this.personality = personality;
        this.memory = memory;
        this.recall = recall;
        this.recallService = recallService;
    }

    // ===============================
    // PUBLIC API
    // ===============================
    public String generate(String userInput) {

        // 1️⃣ Recall relevant memories
        List<String> memories = recall.recall(userInput);

        String memoryContext = "";

        if (!memories.isEmpty()) {
            memoryContext =
                    "\nRelevant memories about the user:\n"
                            + String.join("\n", memories)
                            + "\n";
        }

        // 2️⃣ Build intelligent prompt
        String prompt =
                personality.systemPrompt()
                        + memoryContext
                        + "\nUser: " + userInput
                        + "\nShree:";


        try {
            return callOllama(prompt);
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, my brain is warming up...";
        }
    }

    // ===============================
    // OLLAMA HTTP CALL
    // ===============================
    private String callOllama(String prompt) throws IOException {

        Map<String, Object> body = new HashMap<>();
        body.put("model", "phi3");
        body.put("prompt", prompt);
        body.put("stream", false);

        body.put("options", Map.of(
                "temperature", 0.7,
                "stop", List.of("User:", "Shree:")
        ));

        String json = mapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response =
                     client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody =
                    response.body().string();

            JsonNode node =
                    mapper.readTree(responseBody);

            // ✅ THIS IS THE MODEL OUTPUT
            return node.get("response").asText().trim();
        }
    }
}
