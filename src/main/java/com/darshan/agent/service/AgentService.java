package com.darshan.agent.service;

import com.darshan.agent.autonomy.AutonomousScheduler;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.brain.AgentBrain;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.context.ConversationSessionManager;
import com.darshan.agent.dto.AgentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentService.class);

    private final AgentBrain brain;
    private final GoalManager goals;
    private final ConversationSessionManager sessionManager;

    public AgentService(
            AgentBrain brain,
            GoalManager goals,
            ConversationSessionManager sessionManager) {
        this.brain = brain;
        this.goals = goals;
        this.sessionManager = sessionManager;
    }

    /**
     * Process a message with session support.
     * @param input The user's message
     * @param sessionId Optional session ID for conversation continuity
     * @return The agent's response with session ID
     */
    public AgentResponse process(String input, String sessionId) throws Exception {
        long start = System.currentTimeMillis();
        log.info("[AgentService] Processing request, sessionId={}, inputLength={}", sessionId, input.length());

        // Pause autonomous processing during user request
        AutonomousScheduler.pause();

        try {
            // Get or create session
            ConversationSession session = sessionManager.getOrCreateSession(sessionId);
            ConversationContext context = session.getContext();

            // Add user message to session history
            sessionManager.addMessage(session, "USER", input);

            // Goal command
            if (input.toLowerCase().startsWith("goal:")) {
                goals.createGoal(input.replace("goal:", "").trim());
                sessionManager.addMessage(session, "AI", "Goal accepted. Shree is now working autonomously.");
                return new AgentResponse(
                        "Goal accepted. Shree is now working autonomously.",
                        false,
                        session.getSessionId()
                );
            }

            // Process through brain with session-specific lesson state
            long brainStart = System.currentTimeMillis();
            AgentResponse response = brain.process(input, context, session.getLessonState());
            long brainElapsed = System.currentTimeMillis() - brainStart;
            log.info("[AgentService] Brain processing completed in {}ms", brainElapsed);

            // Add agent response to session history for persistence
            sessionManager.addMessage(session, "AI", response.getSuggestion());

            long elapsed = System.currentTimeMillis() - start;
            log.info("[AgentService] Request completed in {}ms (total)", elapsed);

            // Return response with session ID
            return new AgentResponse(
                    response.getSuggestion(),
                    response.isApprovalRequired(),
                    session.getSessionId()
            );
        } finally {
            // Always resume autonomous processing
            AutonomousScheduler.resume();
        }
    }

    /**
     * Backward compatible process method (uses fallback context).
     */
    @Deprecated
    public AgentResponse process(String input) throws Exception {
        return process(input, null);
    }
}