package com.darshan.agent.controller;

import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.context.ConversationSessionManager;
import com.darshan.agent.dto.AgentRequest;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.memory.ActivityFeed;
import com.darshan.agent.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService service;
    private final ActivityFeed activityFeed;
    private final ConversationSessionManager sessionManager;

    public AgentController(
            AgentService service,
            ActivityFeed activityFeed,
            ConversationSessionManager sessionManager) {
        this.service = service;
        this.activityFeed = activityFeed;
        this.sessionManager = sessionManager;
    }

    /**
     * Main chat endpoint with session support.
     */
    @PostMapping("/ask")
    public AgentResponse askAgent(@RequestBody AgentRequest request)
            throws Exception {

        String message = request.getMessage();

        if (message == null || message.isBlank()) {
            message = "hello";
        }

        // Process with session ID from request (or null to create new)
        return service.process(message, request.getSessionId());
    }

    /**
     * Get recent activity.
     */
    @GetMapping("/activity")
    public List<String> activity() {
        return activityFeed.getRecentActivity();
    }

    /**
     * Create a new conversation session.
     */
    @PostMapping("/session")
    public Map<String, Object> createSession(@RequestBody(required = false) Map<String, String> payload) {
        String userId = payload != null ? payload.get("userId") : null;
        
        ConversationSession session = sessionManager.createSession(userId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", session.getSessionId());
        result.put("createdAt", session.getCreatedAt());
        result.put("message", "Session created successfully");
        
        return result;
    }

    /**
     * Get session details by ID.
     */
    @GetMapping("/session/{sessionId}")
    public Map<String, Object> getSession(@PathVariable String sessionId) {
        return sessionManager.getSession(sessionId)
                .map(session -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("sessionId", session.getSessionId());
                    result.put("userId", session.getUserId());
                    result.put("createdAt", session.getCreatedAt());
                    result.put("lastAccessedAt", session.getLastAccessedAt());
                    result.put("messageCount", session.getMessageCount());
                    result.put("firstMessage", session.getFirstMessage());
                    result.put("summary", session.getSummary());
                    return result;
                })
                .orElseGet(() -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("error", "Session not found");
                    return result;
                });
    }

    /**
     * List all active sessions.
     */
    @GetMapping("/sessions")
    public List<Map<String, Object>> listSessions() {
        return sessionManager.listActiveSessions().stream()
                .map(session -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("sessionId", session.getSessionId());
                    result.put("userId", session.getUserId());
                    result.put("createdAt", session.getCreatedAt());
                    result.put("lastAccessedAt", session.getLastAccessedAt());
                    result.put("messageCount", session.getMessageCount());
                    result.put("firstMessage", session.getFirstMessage());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Delete/end a session.
     */
    @DeleteMapping("/session/{sessionId}")
    public Map<String, Object> deleteSession(@PathVariable String sessionId) {
        boolean deleted = sessionManager.endSession(sessionId);
        
        Map<String, Object> result = new HashMap<>();
        if (deleted) {
            result.put("message", "Session deleted successfully");
        } else {
            result.put("error", "Session not found");
        }
        return result;
    }

    /**
     * Get session count.
     */
    @GetMapping("/sessions/count")
    public Map<String, Object> getSessionCount() {
        Map<String, Object> result = new HashMap<>();
        result.put("activeSessions", sessionManager.getActiveSessionCount());
        return result;
    }
}
