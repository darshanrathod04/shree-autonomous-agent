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

@Component
public class LessonEngine {

    private static final String LESSONS_FILE = "lessons.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private final ConversationManager conversationManager;
    private final Map<String, List<String>> lessonChapters = new LinkedHashMap<>();
    private final Map<String, Integer> lessonProgress = new HashMap<>();

    public LessonEngine(@org.springframework.beans.factory.annotation.Qualifier("lessonConversationManager") ConversationManager conversationManager) {
        this.conversationManager = conversationManager;
    }

    @PostConstruct
    public void init() {
        load();
    }

    // ==================== LESSON MANAGEMENT ====================

    /**
     * Start a new lesson on a topic.
     */
    public String startLesson(String topic) {
        String topicLower = topic.toLowerCase();
        conversationManager.setActiveTopic(topic);
        conversationManager.setLessonName(topic);
        conversationManager.setChapterNumber(1);

        // Load or create lesson plan
        if (!lessonChapters.containsKey(topicLower)) {
            List<String> chapters = generateDefaultChapters(topic);
            lessonChapters.put(topicLower, chapters);
            lessonProgress.put(topicLower, 0);
        }

        conversationManager.save();
        save();

        List<String> chapters = lessonChapters.get(topicLower);
        String chapterTitle = chapters.isEmpty() ? "Introduction" : chapters.get(0);

        return "📘 Starting lesson: " + topic + "\n"
                + "Chapter 1: " + chapterTitle + "\n"
                + "I'll teach you step by step. Say 'next' to continue.";
    }

    /**
     * Go to next chapter.
     */
    public String nextChapter() {
        String topic = conversationManager.getLessonName();
        if (topic == null) {
            return "No active lesson. Say 'learn <topic>' to start one.";
        }

        int currentChapter = conversationManager.getChapterNumber();
        List<String> chapters = lessonChapters.get(topic.toLowerCase());

        if (chapters == null || currentChapter >= chapters.size()) {
            return "🎉 Congratulations! You've completed the " + topic + " lesson. "
                    + "Say 'quiz me' to test your knowledge or 'learn' a new topic.";
        }

        // Mark previous as completed
        conversationManager.addCompletedChapter("Ch." + currentChapter + ": " + chapters.get(currentChapter - 1));

        // Move to next
        currentChapter++;
        conversationManager.setChapterNumber(currentChapter);

        String chapterTitle = chapters.get(currentChapter - 1);
        conversationManager.setCurrentObjective("Teach chapter " + currentChapter + ": " + chapterTitle);

        lessonProgress.put(topic.toLowerCase(), currentChapter - 1);
        conversationManager.save();
        save();

        StringBuilder sb = new StringBuilder();
        sb.append("📘 Chapter ").append(currentChapter).append(": ").append(chapterTitle).append("\n");
        sb.append("Progress: ").append(currentChapter).append("/").append(chapters.size()).append(" chapters\n");
        sb.append("Say 'next' to continue or 'previous' to go back.");

        return sb.toString();
    }

    /**
     * Go to previous chapter.
     */
    public String previousChapter() {
        String topic = conversationManager.getLessonName();
        if (topic == null) {
            return "No active lesson to go back to.";
        }

        int currentChapter = conversationManager.getChapterNumber();
        if (currentChapter <= 1) {
            return "You're already at the first chapter.";
        }

        currentChapter--;
        conversationManager.setChapterNumber(currentChapter);

        List<String> chapters = lessonChapters.get(topic.toLowerCase());
        String chapterTitle = chapters != null && currentChapter <= chapters.size()
                ? chapters.get(currentChapter - 1) : "Topic " + currentChapter;

        conversationManager.save();
        save();

        return "📘 Going back to Chapter " + currentChapter + ": " + chapterTitle;
    }

    /**
     * Get lesson summary.
     */
    public String getSummary() {
        String topic = conversationManager.getLessonName();
        if (topic == null) {
            return "No active lesson.";
        }

        int current = conversationManager.getChapterNumber();
        List<String> chapters = lessonChapters.get(topic.toLowerCase());
        int total = chapters != null ? chapters.size() : 0;
        List<String> completed = conversationManager.getCompletedChapters();

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
     * Generate quiz questions for the current topic.
     */
    public String quizMode() {
        String topic = conversationManager.getLessonName();
        if (topic == null) {
            return "No active lesson to quiz on. Say 'learn <topic>' first.";
        }

        int completed = conversationManager.getCompletedChapters().size();

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