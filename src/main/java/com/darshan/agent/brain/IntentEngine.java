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
        String rawInput = input;
        String text = input.toLowerCase().trim();
        
        System.out.println("[IntentEngine] RAW INPUT: '" + rawInput + "' | NORMALIZED: '" + text + "'");

        // Identity detection (highest priority)
        if (text.contains("who am i")
                || text.contains("what is my name")
                || text.contains("mera naam kya")
                || text.equals("whoami")) {
            System.out.println("[IntentEngine] DETECTED: WHO_AM_I");
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

        if(text.contains("roadmap")) {
            return "ROADMAP_REQUEST";
        }

        // Planning/roadmap intent
        if (text.contains("become a") || text.contains("plan") || text.contains("roadmap")
                || text.contains("career path") || text.contains("learning path")
                || text.contains("steps to") || text.contains("how do i become")
                || text.contains("how to become")) {
            return "PLAN";
        }

        // Greeting
        if (text.contains("hello") || text.contains("hi") || text.contains("hey"))
            return "GREETING";

        // Time
        if (text.contains("what time") || text.contains("current time")
                || text.contains("what's the time") || text.equals("time")
                || text.contains("what is the time") || text.contains("tell me the time")
                || text.contains("kitne baje") || text.contains("time kya hai")) {
            return "TIME";
        }

        // Weather
        if (text.contains("weather"))
            return "WEATHER";

        // Reminder
        if (text.contains("remind") || text.contains("reminder"))
            return "REMINDER";

        if(text.contains("what should i do next")
                || text.contains("what next")
                || text.contains("continue roadmap")
                || text.equals("next")
                || text.equals("ok next")
                || text.equals("okay next")
                || text.equals("next please")
                || text.contains("next task")
                || text.contains("next step")) {

            return "NEXT_STEP";
        }

        // Complete task
        if (text.equals("done")
                || text.equals("completed")
                || text.equals("finished")
                || text.contains("task completed")
                || text.contains("mark complete")
                || text.equals("mark done")) {
            return "COMPLETE_TASK";
        }

        // Progress
        if (text.contains("progress")
                || text.contains("show progress")
                || text.contains("how much completed")
                || text.contains("roadmap progress")
                || text.equals("how much done")) {
            return "PROGRESS";
        }

        // Current task
        if (text.contains("current task")
                || text.contains("what am i doing")
                || text.contains("active task")
                || text.contains("what should i study")
                || text.equals("what should i learn")
                || text.equals("what to study")) {
            return "CURRENT_TASK";
        }

        return "DEFAULT";
    }

    private boolean isFollowUp(String text) {
        return text.equals("next")
                || text.equals("ok next")
                || text.equals("okay next")
                || text.equals("next please")
                || text.contains("what next")
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