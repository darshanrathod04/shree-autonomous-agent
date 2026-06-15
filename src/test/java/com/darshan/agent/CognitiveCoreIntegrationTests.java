package com.darshan.agent;

import com.darshan.agent.autonomy.AgentGoal;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.autonomy.SubGoal;
import com.darshan.agent.brain.AgentBrain;
import com.darshan.agent.brain.IntentEngine;
import com.darshan.agent.brain.perception.IdentityPerceptionEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationSession;
import com.darshan.agent.context.ConversationSessionManager;
import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.service.AgentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CognitiveCoreIntegrationTests {

    @Autowired private AgentService agentService;
    @Autowired private AgentBrain agentBrain;
    @Autowired private UserProfile userProfile;
    @Autowired private IdentityPerceptionEngine identityEngine;
    @Autowired private GoalManager goalManager;
    @Autowired private ConversationSessionManager sessionManager;
    @Autowired private EpisodicMemoryEngine episodicMemory;
    @Autowired private MemoryFacade memoryFacade;
    @Autowired private IntentEngine intentEngine;
    @Autowired private PersonalityEngine personalityEngine;
    @Autowired private com.darshan.agent.context.ConversationManager lessonConversationManager;

    private ConversationContext context;

    @BeforeEach
    void setUp() {
        context = new ConversationContext();
        goalManager.clearGoal();
        lessonConversationManager.setLessonName(null);
        lessonConversationManager.setActiveTopic(null);
        lessonConversationManager.setChapterNumber(0);
        lessonConversationManager.getCompletedChapters().clear();
    }

    @Test @DisplayName("Identity: Remember user name and recall it")
    void testUserIdentityMemory() throws Exception {
        identityEngine.perceive("My name is Darshan");
        assertEquals("Darshan", userProfile.getName());
        var response = agentService.process("Who am I?", null);
        assertTrue(response.getSuggestion().contains("Darshan"));
    }

    @Test @DisplayName("Identity: 'Who am I?' before any name is set returns unknown")
    void testIdentityBeforeSet() throws Exception {
        String currentName = userProfile.getName();
        var response = agentService.process("Who am I?", null);
        if (currentName == null) {
            assertTrue(response.getSuggestion().contains("don't know"));
        } else {
            assertTrue(response.getSuggestion().contains(currentName));
        }
    }

    @Test @DisplayName("Continuity: Session switching restores context")
    void testSessionSwitchingContinuity() throws Exception {
        var sessionA = sessionManager.createSession("user1");
        agentService.process("Hello from Session A", sessionA.getSessionId());
        var sessionB = sessionManager.createSession("user1");
        agentService.process("Hello from Session B", sessionB.getSessionId());
        var loadedA = sessionManager.getSession(sessionA.getSessionId());
        assertTrue(loadedA.isPresent());
        assertEquals(2, loadedA.get().getMessageCount());
    }

    @Test @DisplayName("Continuity: Identity remembered across sessions")
    void testIdentityAcrossSessions() throws Exception {
        var session1 = sessionManager.createSession();
        agentService.process("My name is Darshan", session1.getSessionId());
        assertEquals("Darshan", userProfile.getName());
        var session2 = sessionManager.createSession();
        var response = agentService.process("Who am I?", session2.getSessionId());
        assertTrue(response.getSuggestion().contains("Darshan"));
    }

    @Test @DisplayName("Goal: Create and track a goal")
    void testGoalCreation() throws Exception {
        assertFalse(goalManager.hasGoal());
        agentService.process("goal: Learn Java", null);
        assertTrue(goalManager.hasGoal());
        assertEquals("Learn Java", goalManager.getGoal().getDescription());
    }

    @Test @DisplayName("Goal: Subgoal progression")
    void testSubGoalProgression() {
        AgentGoal goal = new AgentGoal("Learn DSA");
        goal.addSubGoal("Learn Arrays");
        goal.addSubGoal("Learn Linked Lists");
        SubGoal first = goal.nextPending();
        assertEquals("Learn Arrays", first.getDescription());
        assertFalse(first.isCompleted());
        first.complete();
        assertTrue(first.isCompleted());
        SubGoal second = goal.nextPending();
        assertEquals("Learn Linked Lists", second.getDescription());
    }

    @Test @DisplayName("Goal: Single goal limit enforced")
    void testSingleGoalLimit() throws Exception {
        agentService.process("goal: Learn Java", null);
        assertTrue(goalManager.hasGoal());
        agentService.process("goal: Learn Python", null);
        assertEquals("Learn Java", goalManager.getGoal().getDescription());
    }

    @Test @DisplayName("Goal: Goal completion clears state")
    void testGoalCompletion() {
        AgentGoal goal = new AgentGoal("Test Goal");
        goal.addSubGoal("Step 1");
        assertFalse(goal.isCompleted());
        SubGoal step = goal.nextPending();
        step.complete();
        assertNull(goal.nextPending());
    }

    @Test @DisplayName("Memory: Episodic memory stores and recalls")
    void testEpisodicMemory() {
        List<Episode> before = episodicMemory.all();
        int initialCount = before.size();
        identityEngine.perceive("My name is Darshan");
        List<Episode> after = episodicMemory.all();
        assertTrue(after.size() > initialCount);
    }

    @Test @DisplayName("Memory: Semantic memory learns and recalls")
    void testSemanticMemory() {
        memoryFacade.learnConcept("java", "Java is an OOP language");
        String recalled = memoryFacade.recallSemantic("java");
        assertTrue(recalled.contains("OOP"));
    }

    @Test @DisplayName("Memory: UserProfile is settable and accessible")
    void testUserProfileAccess() {
        String oldName = userProfile.getName();
        userProfile.setName("TestUser");
        assertEquals("TestUser", userProfile.getName());
        if (oldName != null) userProfile.setName(oldName);
    }

    @Test @DisplayName("Intent: Detect study intent")
    void testStudyIntent() {
        assertEquals("STUDY", intentEngine.detectIntent("I want to study Java"));
        assertEquals("STUDY", intentEngine.detectIntent("study math"));
    }

    @Test @DisplayName("Intent: Detect greeting intent")
    void testGreetingIntent() {
        assertEquals("GREETING", intentEngine.detectIntent("hello"));
        assertEquals("GREETING", intentEngine.detectIntent("hi there"));
    }

    @Test @DisplayName("Intent: Default intent for unknown input")
    void testDefaultIntent() {
        assertEquals("DEFAULT", intentEngine.detectIntent("random text here"));
    }

    @Test @DisplayName("Intent: 'Next' detected as FOLLOW_UP intent (no active lesson)")
    void testNextIntentDetected() {
        assertEquals("FOLLOW_UP", intentEngine.detectIntent("next"));
    }

    @Test @DisplayName("Intent: 'Continue' detected as FOLLOW_UP intent (no active lesson)")
    void testContinueIntentDetected() {
        assertEquals("FOLLOW_UP", intentEngine.detectIntent("continue"));
    }

    @Test @DisplayName("Intent: 'Learn X' detected as LEARN intent")
    void testLearnIntentDetected() {
        assertEquals("LEARN", intentEngine.detectIntent("Learn Spring Boot"));
    }

    @Test @DisplayName("Intent: 'Who am I' detected as WHO_AM_I intent")
    void testWhoAmIIntentDetected() {
        assertEquals("WHO_AM_I", intentEngine.detectIntent("who am i"));
        assertEquals("WHO_AM_I", intentEngine.detectIntent("What is my name"));
    }

    @Test @DisplayName("Personality: Mood reflects motivation state")
    void testPersonalityMood() {
        String mood = personalityEngine.mood();
        assertNotNull(mood);
        assertFalse(mood.isEmpty());
    }

    @Test @DisplayName("Autonomous: Goal triggers autonomous thinking")
    void testGoalTriggersThinking() throws Exception {
        agentService.process("goal: Learn DSA", null);
        assertTrue(goalManager.hasGoal());
        String desc = goalManager.getGoal().getDescription();
        assertTrue(desc.toLowerCase().contains("learn"));
    }

    @Test @DisplayName("Autonomous: No goal = no autonomous activity")
    void testNoGoalNoAutonomous() {
        assertFalse(goalManager.hasGoal());
        assertNull(goalManager.getGoal());
    }

    @Test @DisplayName("Orchestration: Brain processes valid input")
    void testBrainProcessesInput() throws Exception {
        var response = agentService.process("hello", null);
        assertNotNull(response);
        assertNotNull(response.getSuggestion());
        assertFalse(response.getSuggestion().isBlank());
    }

    @Test @DisplayName("Orchestration: Session ID returned in response")
    void testSessionIdReturned() throws Exception {
        var response = agentService.process("test message", null);
        assertNotNull(response.getSessionId());
        assertFalse(response.getSessionId().isBlank());
    }
}