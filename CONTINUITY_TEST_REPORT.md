# Continuity Test Report

**Date:** 13 June 2026  
**Project:** Shree Autonomous Agent  
**Component:** Conversation Continuity & Identity Memory

---

## 1. Identity Memory Audit

### 1.1 Identity Detection Path

**File:** `brain/perception/IdentityPerceptionEngine.java`

Extracts name via regex patterns:
```java
Pattern.compile("(?:my name is|i am|mera naam|mai)\\s+([a-zA-Z ]+)")
```

Stores name in two places:
- `UserProfile.setName(name)` — in-memory only
- `EpisodicMemoryEngine.store(episode)` — via local `episodes` list

### 1.2 Identity Recall Path

**File:** `skills/ChatSkill.java` (line 98-113)

```java
if (isIdentityQuestion(input)) {
    String name = userProfile.getName();
    if (name != null && !name.isBlank()) {
        return "Your name is " + name + ". I remember you.";
    }
    return "I don't know your name yet.";
}
```

### 1.3 Critical Flaw: Identity Lost on Restart

`UserProfile` is in-memory only with NO persistence. After application restart:
- `UserProfile.name` is null
- Episodic memory (also in-memory) is empty
- The agent replies: **"I don't know your name yet."**

---

## 2. Topic Continuity Audit

### 2.1 Topic Tracking

**File:** `ConversationContext.java`

```java
private String topic = null;
private String currentTopic;
```

- `topic` is set but **never used** in the main pipeline
- `currentTopic` is set but **never read** by ChatSkill or AgentBrain
- No automatic topic extraction from user input

### 2.2 Topic in Prompt

The `ChatSkill.execute()` builds a prompt that includes:
```java
context.getConversationSummary()  // Last 10 messages
```

This allows the LLM to infer the topic from recent history, but there is **no explicit topic memory** stored or recalled.

### 2.3 Session Switching Restores State

**File:** `ConversationSessionManager.getOrCreateSession()`

```java
public ConversationSession getOrCreateSession(String sessionId) {
    Optional<ConversationSession> existing = repository.findById(sessionId);
    if (existing.isPresent()) {
        return existing.get();  // Full state restored from JSON
    }
    return createSession();
}
```

✅ Session switching correctly restores full conversation state from `sessions/{id}.json`.

---

## 3. Lesson Continuity Audit

### 3.1 Lesson Tracking

**File:** `skills/StudySkill.java`

```java
public String execute(String input, ConversationContext context) {
    return "Let's start studying 📘";  // Always starts fresh!
}
```

The `StudySkill` returns a static response. It does NOT:
- Track what topic was being studied
- Remember progress within a lesson
- Resume from where the user left off

### 3.2 "Next" Command Handling

The `IntentEngine` does not detect "next" as a command:
```java
public String detectIntent(String input) {
    // No check for "next", "continue", "resume"
    return "DEFAULT";  // Falls to CHAT skill
}
```

When the user says "Next", the system routes to `ChatSkill` (as DEFAULT intent), which calls the LLM. There is **no structured lesson progression**.

---

## 4. Goal Continuity Audit

### 4.1 Goal Persistence

**File:** `GoalManager.java`

```java
private AgentGoal currentGoal;  // In-memory only
```

**After restart:** `currentGoal` is null. All goals, subgoals, and completion status are lost.

### 4.2 Goal Resumption

Since `GoalManager` has no persistence:
- Previous goals are forgotten
- Subgoal completion states are lost
- The autonomous loop stops (no goal = no loop execution)

---

## 5. Test Case Analysis

### 5.1 Test: "My name is Darshan" → later "Who am I?"

| Step | Expected | Actual Behavior | Status |
|------|----------|----------------|--------|
| User says "My name is Darshan" | Store name | IdentityPerceptionEngine extracts "Darshan" → sets UserProfile.name | ✅ Works |
| User says "Who am I?" | Return "You are Darshan." | ChatSkill checks isIdentityQuestion → returns name from UserProfile | ✅ Works |
| Restart application | Still remember name | UserProfile.name = null on restart → returns "I don't know your name yet." | ❌ Fails |

### 5.2 Test: "Learn Spring Boot" → "Next"

| Step | Expected | Actual Behavior | Status |
|------|----------|----------------|--------|
| User says "Learn Spring Boot" | Start lesson | IntentEngine detects "study" → routes to StudySkill → "Let's start studying 📘" | ⚠️ Static response |
| User says "Next" | Continue lesson | IntentEngine returns "DEFAULT" → ChatSkill calls LLM with conversation summary | ⚠️ Works via LLM context |
| User switches session | Resume lesson | Session restored from JSON → conversation summary includes previous messages | ✅ Works |
| Restart application | Resume lesson | Current topic lost, no lesson state persistence | ❌ Fails |

### 5.3 Test: Session Switching

| Step | Expected | Actual Behavior | Status |
|------|----------|----------------|--------|
| Create Session A | Has context A | SessionRepository saves A to `sessions/{id}.json` | ✅ Works |
| Create Session B | Has context B | Separate file for B | ✅ Works |
| Switch to A | Context A restored | Session loaded from JSON file | ✅ Works |
| Switch to B | Context B restored | Session loaded from JSON file | ✅ Works |

---

## 6. Summary of Continuity

| Feature | Current Implementation | Status |
|---------|----------------------|--------|
| User identity remembered (same session) | UserProfile + ChatSkill hard rule | ✅ Works |
| User identity survives restart | UserProfile not persisted | ❌ Fails |
| Current topic remembered | topic/currentTopic fields exist but unused | ❌ Not implemented |
| Current lesson remembered | StudySkill has no state | ❌ Not implemented |
| Current goal remembered | GoalManager in-memory only | ❌ Fails on restart |
| Session switching restores state | SessionRepository JSON persistence | ✅ Works |
| Cross-session identity sharing | UserProfile global but in-memory | ❌ Fails on restart |