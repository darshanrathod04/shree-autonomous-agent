package com.darshan.agent.context;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("lessonConversationManager")
public class ConversationManager {

    private static final String STATE_FILE = "conversation_state.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    private String activeTopic = null;
    private int chapterNumber = 0;
    private String currentObjective = null;
    private String lessonName = null;
    private List<String> pendingFollowups = new ArrayList<>();
    private List<String> completedChapters = new ArrayList<>();
    private List<String> lessonTopicsCovered = new ArrayList<>();
    private Instant lastActivityAt;

    @PostConstruct
    public void init() {
        lastActivityAt = Instant.now();
        load();
    }

    // ==================== TOPIC MANAGEMENT ====================

    public String getActiveTopic() {
        return activeTopic;
    }

    public void setActiveTopic(String topic) {
        this.activeTopic = topic;
        this.lastActivityAt = Instant.now();
        save();
    }

    // ==================== LESSON PROGRESS ====================

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
        this.lastActivityAt = Instant.now();
        save();
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
        this.lastActivityAt = Instant.now();
        save();
    }

    public String getCurrentObjective() {
        return currentObjective;
    }

    public void setCurrentObjective(String objective) {
        this.currentObjective = objective;
        this.lastActivityAt = Instant.now();
        save();
    }

    public List<String> getPendingFollowups() {
        return pendingFollowups;
    }

    public void addPendingFollowup(String followup) {
        if (pendingFollowups == null) {
            pendingFollowups = new ArrayList<>();
        }
        pendingFollowups.add(followup);
        save();
    }

    public String popNextFollowup() {
        if (pendingFollowups != null && !pendingFollowups.isEmpty()) {
            String next = pendingFollowups.remove(0);
            save();
            return next;
        }
        return null;
    }

    public List<String> getCompletedChapters() {
        return completedChapters;
    }

    public void addCompletedChapter(String chapter) {
        if (completedChapters == null) {
            completedChapters = new ArrayList<>();
        }
        completedChapters.add(chapter);
        save();
    }

    public List<String> getLessonTopicsCovered() {
        return lessonTopicsCovered;
    }

    public void addTopicCovered(String topic) {
        if (lessonTopicsCovered == null) {
            lessonTopicsCovered = new ArrayList<>();
        }
        if (!lessonTopicsCovered.contains(topic)) {
            lessonTopicsCovered.add(topic);
        }
        save();
    }

    // ==================== LESSON NAVIGATION ====================

    public String nextChapter() {
        chapterNumber++;
        lastActivityAt = Instant.now();
        save();
        return "Continuing to chapter " + chapterNumber;
    }

    public String previousChapter() {
        if (chapterNumber > 1) {
            chapterNumber--;
        }
        lastActivityAt = Instant.now();
        save();
        return "Going back to chapter " + chapterNumber;
    }

    // ==================== STATE QUERIES ====================

    public boolean hasActiveTopic() {
        return activeTopic != null && !activeTopic.isBlank();
    }

    public boolean hasActiveLesson() {
        return lessonName != null && !lessonName.isBlank();
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public void touch() {
        lastActivityAt = Instant.now();
        save();
    }

    public String buildProgressSummary() {
        StringBuilder sb = new StringBuilder();
        if (activeTopic != null) {
            sb.append("Active topic: ").append(activeTopic);
        }
        if (lessonName != null) {
            sb.append("\nLesson: ").append(lessonName);
        }
        if (chapterNumber > 0) {
            sb.append("\nCurrent chapter: ").append(chapterNumber);
        }
        if (currentObjective != null) {
            sb.append("\nCurrent objective: ").append(currentObjective);
        }
        if (completedChapters != null && !completedChapters.isEmpty()) {
            sb.append("\nCompleted chapters: ").append(completedChapters.size());
        }
        if (pendingFollowups != null && !pendingFollowups.isEmpty()) {
            sb.append("\nPending followups: ").append(pendingFollowups.size());
        }
        return sb.toString();
    }

    // ==================== PERSISTENCE ====================

    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("activeTopic", activeTopic);
            data.put("chapterNumber", chapterNumber);
            data.put("currentObjective", currentObjective);
            data.put("lessonName", lessonName);
            data.put("pendingFollowups", pendingFollowups);
            data.put("completedChapters", completedChapters);
            data.put("lessonTopicsCovered", lessonTopicsCovered);
            data.put("lastActivityAt", lastActivityAt != null ? lastActivityAt.toString() : null);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(STATE_FILE), data);
        } catch (IOException e) {
            System.err.println("Failed to save conversation state: " + e.getMessage());
        }
    }

    public synchronized void load() {
        try {
            File file = new File(STATE_FILE);
            if (!file.exists()) return;

            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});

            this.activeTopic = (String) data.get("activeTopic");
            this.chapterNumber = data.containsKey("chapterNumber")
                    ? ((Number) data.get("chapterNumber")).intValue() : 0;
            this.currentObjective = (String) data.get("currentObjective");
            this.lessonName = (String) data.get("lessonName");

            if (data.containsKey("pendingFollowups") && data.get("pendingFollowups") instanceof List) {
                this.pendingFollowups = new ArrayList<>((List<String>) data.get("pendingFollowups"));
            }
            if (data.containsKey("completedChapters") && data.get("completedChapters") instanceof List) {
                this.completedChapters = new ArrayList<>((List<String>) data.get("completedChapters"));
            }
            if (data.containsKey("lessonTopicsCovered") && data.get("lessonTopicsCovered") instanceof List) {
                this.lessonTopicsCovered = new ArrayList<>((List<String>) data.get("lessonTopicsCovered"));
            }

            System.out.println("📂 Conversation state restored: topic=" + activeTopic
                    + ", chapter=" + chapterNumber + ", lesson=" + lessonName);
        } catch (IOException e) {
            System.err.println("Failed to load conversation state: " + e.getMessage());
        }
    }
}