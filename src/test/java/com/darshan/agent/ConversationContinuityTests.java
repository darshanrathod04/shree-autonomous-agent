package com.darshan.agent;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.brain.IntentEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationManager;
import com.darshan.agent.context.LessonEngine;
import com.darshan.agent.context.LessonState;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.personality.PersonalityEngine;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ConversationContinuityTests {

    @Autowired
    private IntentEngine intentEngine;

    @Autowired
    private ConversationManager conversationManager;

    @Autowired
    private LessonEngine lessonEngine;

    @Autowired
    private UserProfile userProfile;

    @Autowired
    private GoalManager goalManager;

    @Autowired
    private PersonalityEngine personalityEngine;

    // Per-session LessonState for testing lesson navigation flows
    private LessonState testLessonState;

    @BeforeEach
    void setUp() {
        goalManager.clearGoal();
        // Clear lesson state to avoid test pollution
        conversationManager.setLessonName(null);
        conversationManager.setActiveTopic(null);
        conversationManager.setChapterNumber(0);
        conversationManager.getCompletedChapters().clear();
        // Clear teaching style to avoid personality mode pollution
        userProfile.setTeachingStyle(null);
        // Create fresh per-session lesson state for each test
        testLessonState = new LessonState();
    }

    // =========================================================
    // 1. LESSON INTENT DETECTION
    // =========================================================

    @Test
    @DisplayName("learn spring boot → LEARN intent")
    void testLearnIntent() {
        assertEquals("LEARN", intentEngine.detectIntent("learn spring boot"));
    }

    @Test
    @DisplayName("learn java → LEARN intent")
    void testLearnJavaIntent() {
        assertEquals("LEARN", intentEngine.detectIntent("learn java"));
    }

    @Test
    @DisplayName("teach me python → LEARN intent")
    void testTeachMeIntent() {
        assertEquals("LEARN", intentEngine.detectIntent("teach me python"));
    }

    // =========================================================
    // 2. CONTINUE / NEXT INTENTS
    // =========================================================

    @Test
    @DisplayName("next → CONTINUE when active lesson")
    void testNextContinueWithLesson() {
        lessonEngine.startLesson("Java");
        assertEquals("CONTINUE", intentEngine.detectIntent("next"));
    }

    @Test
    @DisplayName("continue → CONTINUE when active lesson")
    void testContinueIntent() {
        lessonEngine.startLesson("Java");
        assertEquals("CONTINUE", intentEngine.detectIntent("continue"));
    }

    @Test
    @DisplayName("go on → CONTINUE when active lesson")
    void testGoOnContinue() {
        lessonEngine.startLesson("Java");
        assertEquals("CONTINUE", intentEngine.detectIntent("go on"));
    }

    @Test
    @DisplayName("tell me more → CONTINUE when active lesson")
    void testTellMeMoreContinue() {
        lessonEngine.startLesson("Java");
        assertEquals("CONTINUE", intentEngine.detectIntent("tell me more"));
    }

    @Test
    @DisplayName("previous → PREVIOUS intent")
    void testPreviousIntent() {
        assertEquals("PREVIOUS", intentEngine.detectIntent("previous"));
    }

    // =========================================================
    // 3. LESSON NAVIGATION (learn → next → continue)
    // =========================================================

    @Test
    @DisplayName("Learn → next → continue flow")
    void testLessonNavigationFlow() {
        // Start lesson
        String startResult = lessonEngine.startLesson("Spring Boot");
        assertTrue(startResult.contains("Spring Boot"), "Should start Spring Boot lesson");

        // Next chapter
        String nextResult = lessonEngine.nextChapter();
        assertTrue(nextResult.contains("Chapter 2"), "Should advance to Chapter 2");

        // Continue
        String continueResult = lessonEngine.nextChapter();
        assertTrue(continueResult.contains("Chapter 3"), "Should advance to Chapter 3");

        // Summary
        String summary = lessonEngine.getSummary();
        assertTrue(summary.contains("Spring Boot"), "Summary should mention topic");
        assertTrue(summary.contains("2"), "Should show 2 completed chapters");
    }

    @Test
    @DisplayName("Previous chapter navigation")
    void testPreviousNavigation() {
        lessonEngine.startLesson("Java");
        lessonEngine.nextChapter();
        lessonEngine.nextChapter();

        String prevResult = lessonEngine.previousChapter();
        assertTrue(prevResult.contains("Chapter 2"), "Should go back to Chapter 2");
    }

    @Test
    @DisplayName("Quiz mode after learning")
    void testQuizAfterLearning() {
        lessonEngine.startLesson("DSA");
        lessonEngine.nextChapter();
        lessonEngine.nextChapter();

        String quizResult = lessonEngine.quizMode();
        assertTrue(quizResult.contains("Quiz"), "Should be in quiz mode");
        assertTrue(quizResult.contains("DSA"), "Quiz should be about DSA");
    }

    // =========================================================
    // 4. WHO AM I
    // =========================================================

    @Test
    @DisplayName("who am i → WHO_AM_I intent")
    void testWhoAmI() {
        assertEquals("WHO_AM_I", intentEngine.detectIntent("who am i"));
    }

    @Test
    @DisplayName("what is my name → WHO_AM_I intent")
    void testWhatIsMyName() {
        assertEquals("WHO_AM_I", intentEngine.detectIntent("what is my name"));
    }

    // =========================================================
    // 5. GOAL AWARE QUERIES
    // =========================================================

    @Test
    @DisplayName("what are my goals → GOAL_QUERY intent")
    void testGoalQuery() {
        assertEquals("GOAL_QUERY", intentEngine.detectIntent("what are my goals"));
    }

    @Test
    @DisplayName("my goals → GOAL_QUERY intent")
    void testMyGoals() {
        assertEquals("GOAL_QUERY", intentEngine.detectIntent("my goals"));
    }

    @Test
    @DisplayName("goal status → GOAL_QUERY intent")
    void testGoalStatus() {
        assertEquals("GOAL_QUERY", intentEngine.detectIntent("goal status"));
    }

    // =========================================================
    // 6. PERSONALITY MODE SWITCHING
    // =========================================================

    @Test
    @DisplayName("Personality mode: TEACHER when lesson active (via detectMode)")
    void testPersonalityTeacherMode() {
        // PersonalityEngine no longer reads global lesson state.
        // Use detectMode(LessonState) for per-session personality detection.
        LessonState lessonState = new LessonState();
        lessonState.setActiveTopic("Java");
        lessonState.setLessonName("Java");
        assertEquals(PersonalityEngine.Mode.TEACHER, personalityEngine.detectMode(lessonState));
    }

    @Test
    @DisplayName("Personality mode: COACH when goal active")
    void testPersonalityCoachMode() {
        goalManager.createGoal("Become Java Developer");
        personalityEngine.applyPersonality("test");
        assertEquals(PersonalityEngine.Mode.COACH, personalityEngine.getCurrentMode());
    }

    @Test
    @DisplayName("Personality mode: ASSISTANT by default")
    void testPersonalityAssistantDefault() {
        personalityEngine.applyPersonality("test");
        assertEquals(PersonalityEngine.Mode.ASSISTANT, personalityEngine.getCurrentMode());
    }

    // =========================================================
    // 7. LESSON RESUME AFTER RESTART
    // =========================================================

    @Test
    @DisplayName("Lesson state per-session isolation")
    void testLessonPerSessionIsolation() {
        // Create two independent sessions
        LessonState sessionA = new LessonState();
        LessonState sessionB = new LessonState();

        // Session A learns Java
        lessonEngine.startLesson("Java", sessionA);
        lessonEngine.nextChapter(sessionA);  // ch2
        lessonEngine.nextChapter(sessionA);  // ch3

        assertEquals("Java", sessionA.getLessonName());
        assertEquals(3, sessionA.getChapterNumber());
        assertTrue(sessionA.hasActiveLesson());

        // Session B is normal chat (no lesson)
        assertFalse(sessionB.hasActiveLesson());
        assertNull(sessionB.getLessonName());

        // Session B learns Spring independently
        lessonEngine.startLesson("Spring", sessionB);
        assertEquals("Spring", sessionB.getLessonName());
        assertEquals(1, sessionB.getChapterNumber());

        // Session A still has Java at chapter 3
        assertEquals("Java", sessionA.getLessonName());
        assertEquals(3, sessionA.getChapterNumber());
    }

    @Test
    @DisplayName("ConversationManager backward-compatible persistence")
    void testConversationManagerPersistence() {
        conversationManager.setActiveTopic("React");
        conversationManager.setLessonName("React");
        conversationManager.setChapterNumber(1);
        conversationManager.setCurrentObjective("Teach React fundamentals");

        assertEquals("React", conversationManager.getActiveTopic());
        assertEquals(1, conversationManager.getChapterNumber());
        assertTrue(conversationManager.hasActiveLesson());

        // Note: conversationManager persistence is legacy global state.
        // Per-session lesson state is now the primary path via LessonState.
        // This test validates the deprecated backward-compat path still works.
        conversationManager.save();
    }

    // =========================================================
    // 8. QUIZ INTENT
    // =========================================================

    @Test
    @DisplayName("quiz me → QUIZ intent")
    void testQuizIntent() {
        assertEquals("QUIZ", intentEngine.detectIntent("quiz me"));
    }

    @Test
    @DisplayName("quiz → QUIZ intent")
    void testQuizIntentShort() {
        assertEquals("QUIZ", intentEngine.detectIntent("quiz"));
    }

    // =========================================================
    // 9. SUMMARY INTENT
    // =========================================================

    @Test
    @DisplayName("summary → SUMMARY intent")
    void testSummaryIntent() {
        assertEquals("SUMMARY", intentEngine.detectIntent("summary"));
    }

    @Test
    @DisplayName("lesson summary → SUMMARY intent")
    void testLessonSummaryIntent() {
        assertEquals("SUMMARY", intentEngine.detectIntent("lesson summary"));
    }
}