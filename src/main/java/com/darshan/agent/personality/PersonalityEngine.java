package com.darshan.agent.personality;

import com.darshan.agent.cognition.MotivationEngine;
import com.darshan.agent.cognition.MotivationState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonalityEngine {

    @Autowired
    private MotivationEngine motivationEngine;

    private final PersonalityProfile profile = new PersonalityProfile();

    public String applyPersonality(String rawReply) {

        String response = rawReply;

        // Tone layer
        if ("friendly".equals(profile.getTone())) {
            response = "😊 " + response;
        }

        // Emotion layer
        if ("calm".equals(profile.getEmotion())) {
            response += " Take your time.";
        }

        // Style layer
        if ("conversational".equals(profile.getStyle())) {
            response = makeConversational(response);
        }

        return response;
    }

    public String mood() {

        MotivationState s = motivationEngine.getState();

        if (s.getFatigue() > 0.7)
            return "tired but determined";

        if (s.getConfidence() > 0.7)
            return "confident and energetic";

        if (s.getMotivation() < 0.3)
            return "reflective and cautious";

        return "focused";
    }

    public ExpressionLevel expressionLevel() {

        MotivationState s = motivationEngine.getState();

        if (s.getFatigue() > 0.7)
            return ExpressionLevel.CALM;

        if (s.getConfidence() > 0.7)
            return ExpressionLevel.ENERGETIC;

        return ExpressionLevel.SUPPORTIVE;
    }

    private String makeConversational(String text) {
        return text.replace(".", "! 🙂");
    }
}
