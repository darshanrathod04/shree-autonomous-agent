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
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class OllamaClient {

    private static final Logger log = LoggerFactory.getLogger(OllamaClient.class);
    private static final String URL = "http://localhost:11434/api/generate";
    private static final String MODEL = "phi3";

    // Instrumentation: track active requests and caller identity
    private static final AtomicInteger activeRequestCount = new AtomicInteger(0);
    private static final ThreadLocal<String> callerIdentity = ThreadLocal.withInitial(() -> "unknown");

    // Separate OkHttpClient per purpose to avoid connection pooling contention
    private final OkHttpClient client =
            new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

    private final OkHttpClient schedulerClient =
            new OkHttpClient.Builder()
                    .connectTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(180, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                    .build();

    /**
     * Get the number of currently active (in-flight) Ollama requests.
     */
    public static int getActiveRequestCount() {
        return activeRequestCount.get();
    }

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

    /**
     * Set caller identity for logging (e.g., "user" or "scheduler").
     */
    public static void setCallerIdentity(String identity) {
        callerIdentity.set(identity);
    }

    // ===============================
    // PUBLIC API - Scheduler-specific call (uses dedicated client & tracking)
    // ===============================
    public String generateScheduler(String prompt) {
        activeRequestCount.incrementAndGet();
        callerIdentity.set("scheduler");
        long start = System.currentTimeMillis();
        String threadName = Thread.currentThread().getName();
        log.info("[Ollama] scheduler-generate START on thread='{}', promptLength={}, activeRequests={}",
                threadName, prompt.length(), activeRequestCount.get());

        try {
            String result = callOllamaWithClient(schedulerClient, prompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[Ollama] scheduler-generate END in {}ms, responseLength={}, activeRequests={}",
                    elapsed, result.length(), activeRequestCount.get());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[Ollama] scheduler-generate FAILED after {}ms: {}", elapsed, e.getMessage(), e);
            return "Sorry, my brain is warming up...";
        } finally {
            activeRequestCount.decrementAndGet();
        }
    }

    // ===============================
    // PUBLIC API - Legacy (with own memory recall)
    // ===============================
    public String generate(String userInput) {
        activeRequestCount.incrementAndGet();
        callerIdentity.set("user");
        long start = System.currentTimeMillis();
        log.info("[Ollama] generate() START thread='{}', inputLength={}, activeRequests={}",
                Thread.currentThread().getName(), userInput.length(), activeRequestCount.get());

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
            log.info("[Ollama] generate() END in {}ms, responseLength={}, activeRequests={}",
                    elapsed, result.length(), activeRequestCount.get());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[Ollama] generate() FAILED after {}ms: {}", elapsed, e.getMessage(), e);
            return "Sorry, my brain is warming up...";
        } finally {
            activeRequestCount.decrementAndGet();
        }
    }

    // ===============================
    // PUBLIC API - Direct prompt (no duplicate memory recall)
    // Use this when the caller has already built a complete prompt.
    // ===============================
    public String generateDirect(String fullPrompt) {
        activeRequestCount.incrementAndGet();
        callerIdentity.set("user-brain");
        long start = System.currentTimeMillis();
        log.info("[Ollama] generateDirect() START thread='{}', promptLength={}, activeRequests={}",
                Thread.currentThread().getName(), fullPrompt.length(), activeRequestCount.get());

        try {
            String result = callOllama(fullPrompt);
            long elapsed = System.currentTimeMillis() - start;
            log.info("[Ollama] generateDirect() END in {}ms, responseLength={}, activeRequests={}",
                    elapsed, result.length(), activeRequestCount.get());
            return result;
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[Ollama] generateDirect() FAILED after {}ms: {}", elapsed, e.getMessage(), e);
            return "Sorry, my brain is warming up...";
        } finally {
            activeRequestCount.decrementAndGet();
        }
    }

    // ===============================
    // OLLAMA HTTP CALL - User client
    // ===============================
    private String callOllama(String prompt) throws IOException {
        Map<String, Object> body = buildBody(prompt);
        String json = mapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
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

    // ===============================
    // OLLAMA HTTP CALL - Scheduler client (isolated connection pool)
    // ===============================
    private String callOllamaWithClient(OkHttpClient httpClient, String prompt) throws IOException {
        Map<String, Object> body = buildBody(prompt);
        String json = mapper.writeValueAsString(body);

        Request request = new Request.Builder()
                .url(URL)
                .post(RequestBody.create(json, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }
            String responseBody = response.body().string();
            JsonNode node = mapper.readTree(responseBody);
            return node.get("response").asText().trim();
        }
    }

    private Map<String, Object> buildBody(String prompt) {
        Map<String, Object> body = new HashMap<>();
        body.put("model", MODEL);
        body.put("prompt", prompt);
        body.put("stream", false);
        body.put("options", Map.of(
                "temperature", 0.7,
                "num_predict", 1024,
                "stop", List.of("User:")
        ));
        return body;
    }
}