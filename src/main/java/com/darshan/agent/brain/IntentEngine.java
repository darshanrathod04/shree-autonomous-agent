package com.darshan.agent.brain;

import com.darshan.agent.context.ConversationManager;
import org.springframework.stereotype.Component;

@Component
public class IntentEngine {

    private final com.darshan.agent.context.ConversationManager conversationManager;

    public IntentEngine(@org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager") com.darshan.agent.context.ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    public String detectIntent(String input) {

        String text = input.toLowerCase().trim();

        // Identity detection (highest priority)
        if (text.contains("who am i")
                || text.contains("what is my name")
                || text.contains("mera naam kya")
                || text.equals("whoami")) {
            return "WHO_AM_I";
        }

        // Goal query
        if (text.contains("what are my goals")
                || text.contains("my goals")
                || text.contains("goal status")) {
            return "GOAL_QUERY";
        }

        // Summary request
        if (text.equals("summary") || text.contains("lesson summary")) {
            return "SUMMARY";
        }

        // Quiz mode
        if (text.equals("quiz me") || text.contains("quiz time") || text.equals("quiz")) {
            return "QUIZ";
        }

        // Previous chapter
        if (text.equals("previous") || text.equals("go back")
                || text.equals("back") || text.startsWith("previous ")) {
            return "PREVIOUS";
        }

        // Follow-up detection (before lesson detection)
        if (isFollowUp(text)) {
            if (conversationManager.hasActiveLesson()) {
                return "CONTINUE";
            }
            return "FOLLOW_UP";
        }

        // Learning intent (includes "learn", "teach me", etc.)
        if (text.startsWith("learn ")
                || text.contains(" i want to learn ")
                || text.contains("teach me")) {
            return "LEARN";
        }

        // Studying intent
        if (text.contains("study")) {
            return "STUDY";
        }

        // Greeting
        if (text.contains("hello") || text.contains("hi") || text.contains("hey"))
            return "GREETING";

        // Weather
        if (text.contains("weather"))
            return "WEATHER";

        // Reminder
        if (text.contains("remind") || text.contains("reminder"))
            return "REMINDER";

        return "DEFAULT";
    }

    private boolean isFollowUp(String text) {
        return text.equals("next")
                || text.equals("continue")
                || text.equals("resume")
                || text.equals("go on")
                || text.equals("keep going")
                || text.equals("tell me more")
                || text.equals("expand")
                || text.equals("why")
                || text.equals("how")
                || text.equals("elaborate")
                || text.equals("explain")
                || text.startsWith("next ")
                || text.contains("next step")
                || text.contains("next topic");
    }
}