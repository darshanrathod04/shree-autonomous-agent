package com.darshan.agent.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Engine for managing lesson content and progress.
 * Lesson state is now per-session via LessonState parameter,
 * enabling independent lessons across different sessions.
 */
@Component
public class LessonEngine {

    private static final String LESSONS_FILE = "lessons.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Global lesson templates - these are static content definitions, NOT session state
    private final Map<String, List<String>> lessonChapters = new LinkedHashMap<>();
    private final Map<String, Integer> lessonProgress = new HashMap<>();

    // Shared fallback LessonState for backward-compatible no-arg method calls.
    // Tests and legacy code use these overloads. New code should pass a session-specific LessonState.
    private final LessonState fallbackLessonState = new LessonState();
    
    // Global conversation manager for backward compatibility.
    // When backward-compatible no-arg methods are called, they sync state to this manager
    // so that IntentEngine etc. can detect the active lesson.
    private final com.darshan.agent.context.ConversationManager conversationManager;

    public LessonEngine(@org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager") 
                        com.darshan.agent.context.ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @PostConstruct
    public void init() {
        load();
    }

    // ==================== LESSON MANAGEMENT ====================

    /**
     * Start a new lesson on a topic for a specific session's LessonState.
     */
    public String startLesson(String topic, LessonState lessonState) {
        if (lessonState == null) {
            lessonState = new LessonState();
        }
        String topicLower = topic.toLowerCase();
        lessonState.setActiveTopic(topic);
        lessonState.setLessonName(topic);
        lessonState.setChapterNumber(1);

        // Load or create lesson plan
        if (!lessonChapters.containsKey(topicLower)) {
            List<String> chapters = generateDefaultChapters(topic);
            lessonChapters.put(topicLower, chapters);
            lessonProgress.put(topicLower, 0);
        }

        save();

        List<String> chapters = lessonChapters.get(topicLower);
        String chapterTitle = chapters.isEmpty() ? "Introduction" : chapters.get(0);

        return "📘 Starting lesson: " + topic + "\n"
                + "Chapter 1: " + chapterTitle + "\n"
                + "I'll teach you step by step. Say 'next' to continue.";
    }

    /**
     * @deprecated Use {@link #startLesson(String, LessonState)} instead.
     * This method uses a shared fallback LessonState and syncs to global state.
     * It exists only for backward compatibility. New code should pass session.getLessonState().
     */
    @Deprecated
    public String startLesson(String topic) {
        System.err.println("[WARN] ⚠️ LessonEngine.startLesson(String) deprecated - use startLesson(String, LessonState) with session.getLessonState()");
        String result = startLesson(topic, fallbackLessonState);
        syncGlobalLessonState();
        return result;
    }

    /**
     * @deprecated Use {@link #nextChapter(LessonState)} instead.
     */
    @Deprecated
    public String nextChapter() {
        System.err.println("[WARN] ⚠️ LessonEngine.nextChapter() deprecated - use nextChapter(LessonState) with session.getLessonState()");
        String result = nextChapter(fallbackLessonState);
        syncGlobalLessonState();
        return result;
    }

    /**
     * @deprecated Use {@link #previousChapter(LessonState)} instead.
     */
    @Deprecated
    public String previousChapter() {
        System.err.println("[WARN] ⚠️ LessonEngine.previousChapter() deprecated - use previousChapter(LessonState) with session.getLessonState()");
        String result = previousChapter(fallbackLessonState);
        syncGlobalLessonState();
        return result;
    }

    /**
     * @deprecated Use {@link #getSummary(LessonState)} instead.
     */
    @Deprecated
    public String getSummary() {
        System.err.println("[WARN] ⚠️ LessonEngine.getSummary() deprecated - use getSummary(LessonState) with session.getLessonState()");
        return getSummary(fallbackLessonState);
    }

    /**
     * @deprecated Use {@link #quizMode(LessonState)} instead.
     */
    @Deprecated
    public String quizMode() {
        System.err.println("[WARN] ⚠️ LessonEngine.quizMode() deprecated - use quizMode(LessonState) with session.getLessonState()");
        return quizMode(fallbackLessonState);
    }

    /**
     * Syncs the fallback LessonState to the global conversationManager.
     * Only called by deprecated backward-compatible methods.
     */
    @Deprecated
    private void syncGlobalLessonState() {
        if (conversationManager != null) {
            if (fallbackLessonState.hasActiveLesson()) {
                conversationManager.setActiveTopic(fallbackLessonState.getActiveTopic() != null 
                    ? fallbackLessonState.getActiveTopic() : fallbackLessonState.getLessonName());
                conversationManager.setLessonName(fallbackLessonState.getLessonName());
                conversationManager.setChapterNumber(fallbackLessonState.getChapterNumber());
                conversationManager.setCurrentObjective(fallbackLessonState.getCurrentObjective());
                conversationManager.save();
            }
        }
    }

    /**
     * Go to next chapter using session-specific LessonState.
     */
    public String nextChapter(LessonState lessonState) {
        String topic = lessonState.getLessonName();
        if (topic == null) {
            return "No active lesson. Say 'learn <topic>' to start one.";
        }

        int currentChapter = lessonState.getChapterNumber();
        List<String> chapters = lessonChapters.get(topic.toLowerCase());

        if (chapters == null || currentChapter >= chapters.size()) {
            return "🎉 Congratulations! You've completed the " + topic + " lesson. "
                    + "Say 'quiz me' to test your knowledge or 'learn' a new topic.";
        }

        // Mark previous as completed
        lessonState.addCompletedChapter("Ch." + currentChapter + ": " + chapters.get(currentChapter - 1));

        // Move to next
        currentChapter++;
        lessonState.setChapterNumber(currentChapter);

        String chapterTitle = chapters.get(currentChapter - 1);
        lessonState.setCurrentObjective("Teach chapter " + currentChapter + ": " + chapterTitle);

        lessonProgress.put(topic.toLowerCase(), currentChapter - 1);
        save();

        StringBuilder sb = new StringBuilder();
        sb.append("📘 Chapter ").append(currentChapter).append(": ").append(chapterTitle).append("\n");
        sb.append("Progress: ").append(currentChapter).append("/").append(chapters.size()).append(" chapters\n");
        sb.append("Say 'next' to continue or 'previous' to go back.");

        return sb.toString();
    }

    /**
     * Go to previous chapter using session-specific LessonState.
     */
    public String previousChapter(LessonState lessonState) {
        String topic = lessonState.getLessonName();
        if (topic == null) {
            return "No active lesson to go back to.";
        }

        int currentChapter = lessonState.getChapterNumber();
        if (currentChapter <= 1) {
            return "You're already at the first chapter.";
        }

        currentChapter--;
        lessonState.setChapterNumber(currentChapter);

        List<String> chapters = lessonChapters.get(topic.toLowerCase());
        String chapterTitle = chapters != null && currentChapter <= chapters.size()
                ? chapters.get(currentChapter - 1) : "Topic " + currentChapter;

        save();

        return "📘 Going back to Chapter " + currentChapter + ": " + chapterTitle;
    }

    /**
     * Get lesson summary using session-specific LessonState.
     */
    public String getSummary(LessonState lessonState) {
        String topic = lessonState.getLessonName();
        if (topic == null) {
            return "No active lesson.";
        }

        int current = lessonState.getChapterNumber();
        List<String> chapters = lessonChapters.get(topic.toLowerCase());
        int total = chapters != null ? chapters.size() : 0;
        List<String> completed = lessonState.getCompletedChapters();

        StringBuilder sb = new StringBuilder();
        sb.append("📋 Lesson Summary: ").append(topic).append("\n");
        sb.append("Chapters completed: ").append(completed.size()).append("/").append(total).append("\n");
        sb.append("Current chapter: ").append(current).append("\n");

        if (!completed.isEmpty()) {
            sb.append("\nWhat you've learned:\n");
            completed.forEach(c -> sb.append("  ✓ ").append(c).append("\n"));
        }

        if (chapters != null && current <= chapters.size()) {
            sb.append("\nCurrent topic: ").append(chapters.get(current - 1)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Generate quiz questions for the current session's active lesson.
     */
    public String quizMode(LessonState lessonState) {
        String topic = lessonState.getLessonName();
        if (topic == null) {
            return "No active lesson to quiz on. Say 'learn <topic>' first.";
        }

        int completed = lessonState.getCompletedChapters().size();

        if (completed == 0) {
            return "You haven't completed any chapters yet. Start learning first!";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("🎯 Quiz Time: ").append(topic).append("\n\n");
        sb.append("Here are some questions to test your knowledge:\n\n");
        sb.append("1. What are the key concepts of ").append(topic).append("? (").append(completed).append(" chapters covered)\n");
        sb.append("2. Explain the main benefits of ").append(topic).append(".\n");
        sb.append("3. When would you use ").append(topic).append(" in a real project?\n\n");
        sb.append("Try answering these questions to solidify your understanding!");

        return sb.toString();
    }

    // ==================== HELPERS ====================

    private List<String> generateDefaultChapters(String topic) {
        List<String> chapters = new ArrayList<>();
        String t = topic.toLowerCase();

        if (t.contains("java")) {
            chapters.add("Introduction to Java");
            chapters.add("Variables and Data Types");
            chapters.add("Control Flow");
            chapters.add("Object-Oriented Programming");
            chapters.add("Collections Framework");
            chapters.add("Exception Handling");
            chapters.add("File I/O");
            chapters.add("Streams and Lambdas");
        } else if (t.contains("spring")) {
            chapters.add("What is Spring Framework");
            chapters.add("Spring Boot Setup");
            chapters.add("Dependency Injection");
            chapters.add("REST APIs");
            chapters.add("Data Access with JPA");
            chapters.add("Testing");
            chapters.add("Spring Security Basics");
        } else if (t.contains("dsa") || t.contains("data structure")) {
            chapters.add("Arrays and Strings");
            chapters.add("Linked Lists");
            chapters.add("Stacks and Queues");
            chapters.add("Trees");
            chapters.add("Graphs");
            chapters.add("Sorting Algorithms");
            chapters.add("Searching Algorithms");
            chapters.add("Dynamic Programming");
        } else {
            chapters.add("Introduction to " + topic);
            chapters.add("Core Concepts");
            chapters.add("Practical Examples");
            chapters.add("Advanced Topics");
            chapters.add("Best Practices");
        }

        return chapters;
    }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("lessonChapters", lessonChapters);
            data.put("lessonProgress", lessonProgress);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(LESSONS_FILE), data);
        } catch (IOException e) {
            System.err.println("Failed to save lessons: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized void load() {
        try {
            File file = new File(LESSONS_FILE);
            if (!file.exists()) return;

            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            if (data.containsKey("lessonChapters")) {
                Map<String, Object> raw = (Map<String, Object>) data.get("lessonChapters");
                for (Map.Entry<String, Object> entry : raw.entrySet()) {
                    if (entry.getValue() instanceof List) {
                        lessonChapters.put(entry.getKey(), (List<String>) entry.getValue());
                    }
                }
            }

            if (data.containsKey("lessonProgress")) {
                Map<String, Object> raw = (Map<String, Object>) data.get("lessonProgress");
                for (Map.Entry<String, Object> entry : raw.entrySet()) {
                    if (entry.getValue() instanceof Number) {
                        lessonProgress.put(entry.getKey(), ((Number) entry.getValue()).intValue());
                    }
                }
            }

            System.out.println("📚 Lessons loaded: " + lessonChapters.size() + " topics");
        } catch (IOException e) {
            System.err.println("Failed to load lessons: " + e.getMessage());
        }
    }
}