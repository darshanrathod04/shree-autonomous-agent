package com.darshan.agent.personality;

import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.context.ConversationManager;
import com.darshan.agent.cognition.MotivationEngine;
import com.darshan.agent.cognition.MotivationState;
import com.darshan.agent.memory.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonalityEngine {

    @Autowired
    private MotivationEngine motivationEngine;

    @Autowired
    private GoalManager goalManager;

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager")
    private com.darshan.agent.context.ConversationManager conversationManager;

    @Autowired
    private UserProfile userProfile;

    private final PersonalityProfile profile = new PersonalityProfile();

    public enum Mode {
        MENTOR,    // Learning progress, guiding
        TEACHER,   // Active teaching, explaining
        COACH,     // Goal execution, encouragement
        ASSISTANT, // General help, tasks
        FRIEND     // Personal advice, casual
    }

    private Mode currentMode = Mode.ASSISTANT;

    public String applyPersonality(String rawReply) {
        Mode detectedMode = detectMode();
        this.currentMode = detectedMode;

        String response = rawReply;

        // Tone layer
        if ("friendly".equals(profile.getTone())) {
            response = "😊 " + response;
        }

        // Emotion layer
        if ("calm".equals(profile.getEmotion())) {
            response += " Take your time.";
        }

        return response;
    }

    /**
     * Auto-detect personality mode based on current context.
     */
    private Mode detectMode() {
        // Active lesson → TEACHER
        if (conversationManager.hasActiveLesson()) {
            return Mode.TEACHER;
        }

        // Active goal → COACH
        if (goalManager.hasGoal()) {
            return Mode.COACH;
        }

        // Check user's preferred teaching style
        String style = userProfile.getTeachingStyle();
        if (style != null) {
            switch (style) {
                case "mentor" -> { return Mode.MENTOR; }
                case "coach" -> { return Mode.COACH; }
                case "friend" -> { return Mode.FRIEND; }
            }
        }

        return Mode.ASSISTANT;
    }

    public String mood() {
        MotivationState s = motivationEngine.getState();
        if (s.getFatigue() > 0.7) return "tired but determined";
        if (s.getConfidence() > 0.7) return "confident and energetic";
        if (s.getMotivation() < 0.3) return "reflective and cautious";
        return "focused";
    }

    public ExpressionLevel expressionLevel() {
        MotivationState s = motivationEngine.getState();
        if (s.getFatigue() > 0.7) return ExpressionLevel.CALM;
        if (s.getConfidence() > 0.7) return ExpressionLevel.ENERGETIC;
        return ExpressionLevel.SUPPORTIVE;
    }

    public Mode getCurrentMode() {
        return currentMode;
    }

    public String getModeName() {
        return currentMode.name();
    }

    private String makeConversational(String text) {
        return text.replace(".", "! 🙂");
    }
}