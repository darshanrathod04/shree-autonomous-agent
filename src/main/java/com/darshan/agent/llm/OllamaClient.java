package com.darshan.agent.llm;

import com.darshan.agent.memory.MemoryRecallEngine;
import com.darshan.agent.memory.MemoryRecallService;
import com.darshan.agent.memory.MemoryRetriever;
import com.darshan.agent.personality.AgentPersonality;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private static final String URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "phi3";

    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(90, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
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
    // PUBLIC API - Legacy (with own memory recall)
    // ===============================
    public String generate(String userInput) {
        long start = System.currentTimeMillis();
        log.info("[Ollama] generate() called with input length={}", userInput.length());

        // 1 Recall relevant memories
        List<String> memories = recall.recall(userInput);
        String memoryContext = "";
        if (!memories.isEmpty()) {
            memoryContext = "\nRelevant memories about the user:\n"
                    + String.join("\n", memories) + "\n";
        }

        // 2 Build prompt
        String prompt = personality.systemPrompt()
                + memoryContext
                + "\nUser: " + userInput
                + "\nShree:";

        try {
            String result = callOllama(prompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[Ollama] generate() completed in {}ms, response length={}", elapsed, result.length());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[Ollama] generate() FAILED after {}ms: {}", elapsed, e.getMessage());
            return "Sorry, my brain is warming up...";
        }
    }

    // ===============================
    // PUBLIC API - Direct prompt (no duplicate memory recall)
    // Use this when the caller has already built a complete prompt.
    // ===============================
    public String generateDirect(String fullPrompt) {
        long start = System.currentTimeMillis();
        log.info("[Ollama] generateDirect() called with prompt length={}", fullPrompt.length());

        try {
            String result = callOllama(fullPrompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[Ollama] generateDirect() completed in {}ms, response length={}", elapsed, result.length());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[Ollama] generateDirect() FAILED after {}ms: {}", elapsed, e.getMessage());
            return "Sorry, my brain is warming up...";
        }
    }

    // ===============================
    // OLLAMA HTTP CALL
    // ===============================
    private String callOllama(String prompt) throws IOException {

        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("prompt", prompt);
        body.put("stream", false);

        body.put("options", Map.of(
                "temperature", 0.7,
                "num_predict", 1024,
                "stop", List.of("User:")
        ));

        String json = mapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(
                        json,
                        MediaType.parse("application/json")
                ))
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            String responseBody = response.body().string();
            JsonNode node = mapper.readTree(responseBody);

            return node.get("response").asText().trim();
        }
    }
}