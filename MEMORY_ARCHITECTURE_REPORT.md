# Memory Architecture Report

**Date:** 13 June 2026  
**Project:** Shree Autonomous Agent  
**Component:** Memory & Persistence Audit

---

## 1. Memory Systems Overview

### 1.1 Memory Components Identified

| Component | Type | Persistence | Single Entry Point |
|-----------|------|-------------|-------------------|
| `MemoryFacade` | Facade | Orchestrator | ✅ Yes — single gateway |
| `EpisodicMemoryEngine` | Episodic | In-memory list + EpisodeStore | ❌ Dual path (store/remember) |
| `EpisodicRecallEngine` | Recall | Query only | ✅ Uses memory engine |
| `SemanticMemoryEngine` | Semantic | In-memory HashMap | ✅ Single store |
| `MemoryStore` | File-based | `memory.json` | ✅ Single file |
| `UserProfile` | Identity | In-memory only | ❌ No persistence |
| `VectorMemoryStore` | Vector | In-memory | In-memory only |
| `ConversationSession` | Session | JSON files | ✅ SessionRepository |
| `GoalManager` | Goal | In-memory only | ❌ No persistence |

---

## 2. Memory Flow Audit

### 2.1 Conversation Flow as Implemented

```java
AgentService.process(input, sessionId)
  → sessionManager.getOrCreateSession(sessionId)
  → brain.process(input, context)
    → governor.evaluate(input, context)      // Safety gate
    → stateMachine.handle(input, context)     // State transitions
    → recall.recallRelevant(input)            // Memory recall
    → identityPerceptionEngine.perceive(input) // Identity extraction
    → intentEngine.detectIntent(input)         // Intent detection
    → router.route(intent)                     // Skill routing
    → skill.execute(input, context)            // Skill execution
    → meta.evaluate(input, rawReply)           // Self-reflection
    → personality.applyPersonality(rawReply)   // Personality rendering
```

### 2.2 Memory Storage Points

| Trigger | Storage Target | Persisted? |
|---------|---------------|------------|
| User says name | `UserProfile.setName()` | ❌ In-memory only |
| User says name | `EpisodicMemoryEngine.store(episode)` | ❌ In-memory list (not to JSON) |
| Session message | `SessionRepository.save(session)` | ✅ `sessions/{id}.json` |
| Any conversation | `MemoryStore.addConversation()` | ✅ `memory.json` |
| Episodic episode | `EpisodeStore.add(episode)` | ❌ In-memory only |
| Semantic concept | `SemanticMemoryEngine.learn()` | ❌ In-memory HashMap |
| Vector embedding | `VectorMemoryStore.store()` | ❌ In-memory list |

---

## 3. Critical Issues Found

### 3.1 DUPLICATE EPISODE STORAGE (BUG RISK)

**File:** `EpisodicMemoryEngine.java`

Two different storage paths exist:

```java
// Path A: remember() — uses EpisodeStore
public void remember(String input, String response, MetaThought meta) {
    Episode episode = new Episode(type, summary, input, response, score);
    store.add(episode);  // via EpisodeStore
}

// Path B: store() — uses local in-memory list
private final List<Episode> episodes = new ArrayList<>();  // Never populated!
public void store(Episode episode) {
    episodes.add(episode);  // Local list (never used by recall)
}

// all() returns local list, not EpisodeStore!
public List<Episode> all() {
    return episodes;  // ⚠️ Returns empty list if only remember() was called!
}
```

**Bug:** `EpisodicRecallEngine.recallRelevant()` calls `memory.all()` which returns the **local** `episodes` list. But `remember()` stores to `EpisodeStore`, not to the local list. This means:
- Episodes stored via `remember()` → go to EpisodeStore (invisible to recall)
- Episodes stored via `store()` → go to local list (visible to recall)

**Impact:** Memory recall fails to find episodes stored through the normal `remember()` path.

### 3.2 USER PROFILE NOT PERSISTED

**File:** `UserProfile.java`

```java
@Component
public class UserProfile {
    private String name;  // In-memory only — LOST ON RESTART
    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
}
```

No persistence mechanism. The user's name is lost on application restart. The `IdentityPerceptionEngine` also stores the name as an episodic memory, but episodic memory itself is in-memory only (see 3.1).

### 3.3 GOAL MANAGER NOT PERSISTED

**File:** `GoalManager.java`

```java
@Component
public class GoalManager {
    private AgentGoal currentGoal;  // In-memory only — LOST ON RESTART
}
```

Goals, subgoals, and progress are all lost when the application restarts. No serialization to JSON or database.

### 3.4 SEMANTIC MEMORY IN-MEMORY ONLY

**File:** `SemanticMemoryEngine.java`

```java
private final Map<String, SemanticConcept> knowledge = new HashMap<>();
private final Map<String, Concept> concepts = new HashMap<>();
```

All semantic knowledge (concepts, their meanings, frequencies) is stored in HashMaps. Nothing is persisted to disk. On restart, the agent forgets everything it learned.

### 3.5 NO SINGLE MEMORY ENTRY POINT USAGE

While `MemoryFacade` exists as a single entry point, **it is not used by the main pipeline** (`AgentBrain` / `ChatSkill`). Instead, those classes inject individual memory components directly:

```java
// ChatSkill.java — bypasses MemoryFacade
private final EpisodicMemoryEngine episodicMemory;
private final EpisodicRecallEngine recallEngine;
private final SemanticMemoryEngine semantic;
```

This violates the facade pattern and creates potential for inconsistent memory access.

---

## 4. Working Memory in ConversationContext

**File:** `ConversationContext.java`

`ConversationContext` has:
- `workingMemory` — A string field set during recall
- `topic` / `currentTopic` — Topic tracking
- `data` map — Generic key-value store
- `history` — Last 10 conversation entries (truncated)

The context is serialized as part of `ConversationSession` to JSON, so working memory persists within sessions. However, there is no cross-session memory sharing.

---

## 5. Persistence Comparison

| Data Type | Storage | Persists Restart? | Global Across Sessions? |
|-----------|---------|-------------------|------------------------|
| Session messages | JSON files (`sessions/`) | ✅ Yes | ❌ Per session |
| User name | In-memory only | ❌ No | ❌ No |
| Episodic episodes | In-memory only | ❌ No | ❌ No |
| Semantic concepts | In-memory HashMap | ❌ No | ❌ No |
| Active goals | In-memory only | ❌ No | ❌ No |
| Conversation history (JSON) | `memory.json` | ✅ Yes | ✅ Global |
| Personality state | In-memory only | ❌ No | ❌ No |

---

## 6. Recommendations

1. **Fix EpisodeStore vs local list bug**: Unify storage so `all()` returns from EpisodeStore, not the local list.
2. **Persist UserProfile**: Save to `memory.json` or a dedicated `profile.json`.
3. **Persist GoalManager**: Serialize active goals to `goals.json` with subgoal completion states.
4. **Persist SemanticMemory**: Save concepts to JSON file (e.g., `semantic.json`).
5. **Use MemoryFacade everywhere**: Replace direct dependency injection of memory components with the facade.
6. **Cross-session memory**: Enable recall across sessions by querying all session files for relevant memories.
7. **EpisodicMemoryEngine cleanup**: Remove the unused `episodes` local list and the `store()` method to prevent confusion.

---

## 7. Summary

| Requirement | Status | Details |
|-------------|--------|---------|
| Single memory entry point | ⚠️ Partial | MemoryFacade exists but is not used by main pipeline |
| No duplicate storage | ❌ Failed | EpisodicMemoryEngine has dual storage paths (bug) |
| No memory loss between sessions | ❌ Failed | UserProfile, GoalManager, SemanticMemory all in-memory only |
| Memory recall available to all skills | ⚠️ Partial | Skills inject memory directly; no unified recall API |