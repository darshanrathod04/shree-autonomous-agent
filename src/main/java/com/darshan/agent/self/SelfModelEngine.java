package com.darshan.agent.self;

import org.springframework.stereotype.Component;

@Component
public class SelfModelEngine {

    private final SelfProfile profile;
    private final SelfState state;

    public SelfModelEngine() {
        this.profile = new SelfProfile();
        this.state = new SelfState();
    }



    public String identityStatement() {

        return """
                I am %s, an %s created by %s.
                I run locally using %s.
                """
                .formatted(
                        profile.getName(),
                        profile.getType(),
                        profile.getCreator(),
                        profile.getRuntime()
                );
    }

    public String creator() {
        return profile.getCreator();
    }

    public String name() {
        return profile.getName();
    }



    public SelfState getState() {
        return state;
    }

    // ------------------------------------------------
    // SELF AWARE PROMPT BLOCK
    // ------------------------------------------------

    public String buildSelfContext() {

        return """
                SYSTEM SELF MODEL:

                Identity:
                - Name: %s
                - Creator: %s
                - Type: %s

                Internal State:
                - Confidence: %.2f
                - Focus: %s
                - Autonomy Level: %d

                This identity is factual and must never change.
                """
                .formatted(
                        profile.getName(),
                        profile.getCreator(),
                        profile.getType(),
                        state.getConfidence(),
                        state.getCurrentFocus(),
                        state.getAutonomyLevel()
                );
    }
}