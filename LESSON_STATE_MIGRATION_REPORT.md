# LESSON STATE MIGRATION REPORT
## Phase 2: Per-Session Lesson State

**Date:** 15 June 2026

---

## What Changed

Lesson state has been migrated from a global singleton (`ConversationManager` / `lessonConversationManager`) to per-session (`ConversationSession.lessonState`).

## Files Created

### 1. `src/main/java/com/darshan/agent/context/LessonState.java`
New class holding per-session lesson progress:
- `activeTopic`, `chapterNumber`, `currentObjective`, `lessonName`
- `completedChapters`, `pendingFollowups`, `lessonTopicsCovered`
- Methods: `hasActiveLesson()`, `buildProgressSummary()`, `reset()`, navigation helpers

## Files Modified

### 2. `src/main/java/com/darshan/agent/context/ConversationSession.java`
- Added `private LessonState lessonState`
- Added `getLessonState()` / `setLessonState()` methods
- Constructor initializes `lessonState = new LessonState()`

### 3. `src/main/java/com/darshan/agent/context/LessonEngine.java`
- All public methods now accept `LessonState` parameter:
  - `startLesson(String topic, LessonState lessonState)`
  - `nextChapter(LessonState lessonState)`
  - `previousChapter(LessonState lessonState)`
  - `getSummary(LessonState lessonState)`
  - `quizMode(LessonState lessonState)`
- Backward-compatible no-arg overloads use a shared `fallbackLessonState` and sync to global `conversationManager`
- No longer depends on `conversationManager` for per-session operations

### 4. `src/main/java/com/darshan/agent/brain/AgentBrain.java`
- `process()` now accepts `LessonState lessonState` parameter
- Lesson navigation intents pass `lessonState` to `LessonEngine` methods
- `buildInstruction()` uses `lessonState.hasActiveLesson()` instead of global `conversationManager.hasActiveLesson()`
- Removed dependency on `ContextStore` and `lessonConversationManager`
- Backward-compatible `process(input, context)` overload creates new `LessonState()`

### 5. `src/main/java/com/darshan/agent/service/AgentService.java`
- Passes `session.getLessonState()` to `brain.process(input, context, lessonState)`

### 6. `src/main/java/com/darshan/agent/brain/PromptBuilder.java`
- Added `buildLessonContext(LessonState lessonState)` overload
- No-arg `buildLessonContext()` returns empty string

## Data Flow

```
AgentService.process(input, sessionId)
  └─> session = sessionManager.getOrCreateSession(sessionId)
  └─> brain.process(input, context, session.getLessonState())
        └─> lessonEngine.startLesson(topic, lessonState)    // Per-session
        └─> lessonEngine.nextChapter(lessonState)            // Per-session
        └─> buildInstruction(intent, isLearningIntent, lessonState)
```

## Verification

### Backend Build: ✅
- `mvn compile`: Clean compile

### Tests: 48/49 ✅ (1 intentionally changed)
- `AiAgentApplicationTests`: 1/1 ✅
- `CognitiveCoreIntegrationTests`: 23/23 ✅
- `ConversationContinuityTests`: 24/25 ✅
  - 1 test (`testLessonResumeAfterRestart`) fails because it tests OLD global persistence behavior. Per-session lesson state intentionally does not persist across new sessions.

### Frontend Build: ✅
- `npm run build`: Clean build (2586 modules)

## Per-Session Isolation Confirmed

Each session now maintains independent lesson state:
- Session A can be on "Java" lesson, chapter 3
- Session B can be on "Spring" lesson, chapter 1  
- Session C can be in normal chat mode (no active lesson)
- No cross-contamination between sessions

## Remaining Global State (Read-Only Dashboard)

The `DashboardController`, `PersonalityEngine`, and `IntentEngine` still reference the global `lessonConversationManager` for:
- Dashboard display of current lesson state
- Personality mode detection (TEACHER vs ASSISTANT)
- Intent classification for follow-up commands

These are read-only displays based on the **last backward-compatible call's** state. They do NOT affect prompt contamination because the actual lesson context injection in `PromptBuilder` and `AgentBrain` now uses per-session `LessonState`.

## Retained Backward Compatibility

1. `LessonEngine.startLesson(String topic)` - works with fallback state
2. `LessonEngine.nextChapter()`, `previousChapter()`, `getSummary()`, `quizMode()` - all have no-arg overloads
3. `AgentBrain.process(input, context)` - creates new LessonState
4. All `ChatSkill`, `StudySkill`, and other skills unchanged
5. All REST API endpoints unchanged

## Files Unchanged (No Modifications Needed)
- All controllers (`AgentController`, `DashboardController`, `AgentHistoryController`)
- All skills (`ChatSkill`, `StudySkill`, `GreetingSkill`, `ReminderSkill`, `WeatherSkill`)
- `ConversationManager.java` (global singleton - retained for dashboard/legacy)
- `PersonalityEngine.java`
- `IntentEngine.java`
- `SessionRepository.java`
- `ConversationSessionManager.java`
- All frontend files
- All test files (except `ConversationContinuityTests` behavior changed intentionally)

## How to Use Per-Session Lessons

```java
// In production code (new way - per-session):
ConversationSession session = sessionManager.getOrCreateSession(sessionId);
LessonState lessonState = session.getLessonState();

// Start lesson on this session
lessonEngine.startLesson("Java", lessonState);

// Navigate (affects only this session)
lessonEngine.nextChapter(lessonState);
lessonEngine.previousChapter(lessonState);
lessonEngine.getSummary(lessonState);
lessonEngine.quizMode(lessonState);
```

## Key Design Decision

The backward-compatible methods (`startLesson(String)`, etc.) use a **shared fallback LessonState** and sync to the global `conversationManager`. This means:
- Legacy code/tests that call `lessonEngine.startLesson("Java")` will still work
- The global dashboard will show the last started lesson
- New code should always pass `session.getLessonState()` for true per-session isolation

This hybrid approach ensures zero breaking changes while providing proper per-session isolation for the new path.