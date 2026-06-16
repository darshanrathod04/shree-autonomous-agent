package com.darshan.agent.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Per-session lesson progress state.
 * Each ConversationSession maintains its own LessonState,
 * enabling independent lesson tracking across sessions.
 */
public class LessonState {

    private String activeTopic;
    private int chapterNumber;
    private String currentObjective;
    private String lessonName;
    private List<String> completedChapters;
    private List<String> pendingFollowups;
    private List<String> lessonTopicsCovered;

    public LessonState() {
        this.chapterNumber = 0;
        this.completedChapters = new ArrayList<>();
        this.pendingFollowups = new ArrayList<>();
        this.lessonTopicsCovered = new ArrayList<>();
    }

    // ==================== TOPIC MANAGEMENT ====================

    public String getActiveTopic() {
        return activeTopic;
    }

    public void setActiveTopic(String activeTopic) {
        this.activeTopic = activeTopic;
    }

    // ==================== LESSON PROGRESS ====================

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public String getCurrentObjective() {
        return currentObjective;
    }

    public void setCurrentObjective(String currentObjective) {
        this.currentObjective = currentObjective;
    }

    public List<String> getCompletedChapters() {
        return completedChapters;
    }

    public void setCompletedChapters(List<String> completedChapters) {
        this.completedChapters = completedChapters;
    }

    public void addCompletedChapter(String chapter) {
        if (completedChapters == null) {
            completedChapters = new ArrayList<>();
        }
        completedChapters.add(chapter);
    }

    public List<String> getPendingFollowups() {
        return pendingFollowups;
    }

    public void setPendingFollowups(List<String> pendingFollowups) {
        this.pendingFollowups = pendingFollowups;
    }

    public void addPendingFollowup(String followup) {
        if (pendingFollowups == null) {
            pendingFollowups = new ArrayList<>();
        }
        pendingFollowups.add(followup);
    }

    public String popNextFollowup() {
        if (pendingFollowups != null && !pendingFollowups.isEmpty()) {
            return pendingFollowups.remove(0);
        }
        return null;
    }

    public List<String> getLessonTopicsCovered() {
        return lessonTopicsCovered;
    }

    public void setLessonTopicsCovered(List<String> lessonTopicsCovered) {
        this.lessonTopicsCovered = lessonTopicsCovered;
    }

    public void addTopicCovered(String topic) {
        if (lessonTopicsCovered == null) {
            lessonTopicsCovered = new ArrayList<>();
        }
        if (!lessonTopicsCovered.contains(topic)) {
            lessonTopicsCovered.add(topic);
        }
    }

    // ==================== NAVIGATION ====================

    public String nextChapter() {
        chapterNumber++;
        return "Continuing to chapter " + chapterNumber;
    }

    public String previousChapter() {
        if (chapterNumber > 1) {
            chapterNumber--;
        }
        return "Going back to chapter " + chapterNumber;
    }

    // ==================== STATE QUERIES ====================

    public boolean hasActiveTopic() {
        return activeTopic != null && !activeTopic.isBlank();
    }

    public boolean hasActiveLesson() {
        return lessonName != null && !lessonName.isBlank();
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

    /**
     * Reset lesson state for this session.
     */
    public void reset() {
        this.activeTopic = null;
        this.chapterNumber = 0;
        this.currentObjective = null;
        this.lessonName = null;
        if (this.completedChapters != null) this.completedChapters.clear();
        if (this.pendingFollowups != null) this.pendingFollowups.clear();
        if (this.lessonTopicsCovered != null) this.lessonTopicsCovered.clear();
    }
}