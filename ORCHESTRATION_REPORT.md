# Agent Orchestration Report

**Date:** 13 June 2026  
**Project:** Shree Autonomous Agent  
**Component:** Orchestration & Routing Audit

---

## 1. Orchestration Architecture

### 1.1 Components

| Component | File | Role |
|-----------|------|------|
| `AgentService` | `service/AgentService.java` | Session management + goal detection + brain invocation |
| `AgentBrain` | `brain/AgentBrain.java` | Main cognitive pipeline (governor → state → recall → intent → skill → meta → personality) |
| `ChatSkill` | `skills/ChatSkill.java` | Primary skill: LLM call, hard rules, memory, executive control |
| `StudySkill` | `skills/StudySkill.java` | Static study response (not functional) |
| `SkillRouter` | `router/SkillRouter.java` | Routes intent to matching Skill |
| `IntentEngine` | `brain/IntentEngine.java` | Simple keyword-based intent detection |
| `AgentOrchestrator` | `orchestrator/AgentOrchestrator.java` | Parallel agent pipeline (planner → executor → reviewer) |
| `AutonomousLoop` | `autonomy/AutonomousLoop.java` | Autonomous goal execution loop |

---

## 2. Primary Execution Flow (User Message)

```
User Input
  │
  ▼
AgentService.process(input, sessionId)
  ├── sessionManager.getOrCreateSession(sessionId)  ← Session management
  ├── Goal detection (input.startsWith("goal:"))    ← Bypasses brain entirely
  │
  ▼
AgentBrain.process(input, context)
  ├── 1. Governor: safety/stability check
  │     └── Pause/Refuse/Clarify → return early
  ├── 2. StateMachine: handle state transitions
  │     └── Non-null reply → return early
  ├── 3. Memory recall: recall.recallRelevant(input)
  │     └── Sets workingMemory on context
  ├── 4. IdentityPerception: extract user name
  ├── 5. Intent detection: intentEngine.detectIntent(input)
  ├── 6. Skill routing: router.route(intent)
  │     ├── GREETING → GreetingSkill
  │     ├── STUDY → StudySkill
  │     ├── WEATHER → WeatherSkill
  │     ├── REMINDER → ReminderSkill
  │     ├── DEFAULT → ChatSkill (or fallback to swarm)
  │     └── null → DebateSwarmEngine.swarmThink()
  ├── 7. Skill execution (or swarm fallback)
  ├── 8. Meta-cognition: evaluate response quality
  ├── 9. Personality: applyPersonality(rawReply)
  │
  ▼
Return AgentResponse
```

---

## 3. Skill Routing Audit

### 3.1 Skill Registry

**File:** `router/SkillRouter.java`

```java
public Skill route(String intent) {
    for (Skill skill : skills) {
        if (skill.supports(intent)) {
            return skill;
        }
    }
    // Fallback to CHAT
    for (Skill skill : skills) {
        if (skill.supports("CHAT")) {
            return skill;
        }
    }
    throw new RuntimeException("No skill found");
}
```

### 3.2 Skill Implementations

| Skill | Supports | Status |
|-------|----------|--------|
| `ChatSkill` | "CHAT" | ✅ Primary LLM-based skill |
| `StudySkill` | "STUDY" | ⚠️ Static response only ("Let's start studying 📘") |
| `GreetingSkill` | "GREETING" | Presumed exists |
| `WeatherSkill` | "WEATHER" | Presumed exists |
| `ReminderSkill` | "REMINDER" | Presumed exists |
| `DefaultSkill` | None | Presumed exists |

### 3.3 Intent Detection Gaps

**File:** `brain/IntentEngine.java`

```java
public String detectIntent(String input) {
    if (text.contains("hello") || text.contains("hi")) return "GREETING";
    if (text.contains("weather")) return "WEATHER";
    if (input.toLowerCase().contains("summary")) return "SUMMARY";
    if (text.contains("study")) return "STUDY";
    if (input.contains("remind") || input.contains("reminder")) return "REMINDER";
    return "DEFAULT";
}
```

| Command | Intent Detected | Correct? |
|---------|----------------|----------|
| "hello" | GREETING | ✅ Yes |
| "study Java" | STUDY | ✅ Yes |
| "weather in London" | WEATHER | ✅ Yes |
| "remind me" | REMINDER | ✅ Yes |
| "Learn Spring Boot" | DEFAULT | ⚠️ "learn" not detected — falls to ChatSkill |
| "Who am I?" | DEFAULT | ⚠️ Falls to ChatSkill (which handles it via hard rule) |
| "Next" | DEFAULT | ⚠️ Not detected as CONTINUE intent |
| "My name is X" | DEFAULT | ⚠️ Falls to ChatSkill (identity extracted separately) |
| "goal: Learn DSA" | N/A | ✅ Handled before intent detection in AgentService |

---

## 4. Duplicate Execution Paths

### 4.1 Personality Application (Critical Bug)

`PersonalityEngine.applyPersonality()` is called at **two points** in the ChatSkill flow:

1. **ChatSkill.execute()** line 253-254:
   ```java
   if (decision.getAction() == Action.RESPOND)
       return personalityEngine.applyPersonality(response);
   ```
2. **AgentBrain.process()** line 151-153:
   ```java
   String finalReply = personality.applyPersonality(rawReply);
   ```

**Impact:** Double emoji/exclamation mark application. See PERSONALITY_REPORT.md for details.

### 4.2 Memory Recall (Duplicate)

Memory recall happens at **two points** in the ChatSkill flow:

1. **AgentBrain.process()** line 102-105:
   ```java
   String recalledMemory = recall.recallRelevant(input);
   context.setWorkingMemory(recalledMemory);
   ```
2. **ChatSkill.execute()** line 158-159:
   ```java
   String episodic = recallEngine.recallRelevant(input);
   ```

**Impact:** Same recall query executed twice. The first stores to `workingMemory`, but the second re-queries and uses the result independently. The `workingMemory` set by AgentBrain is **never read** by ChatSkill.

### 4.3 Meta-Cognition (Check Exists)

Meta-cognition is applied:
1. **ChatSkill.execute()** line 235-237:
   ```java
   var metaThought = metaCognition.evaluate(input, response);
   ```
2. **AgentBrain.process()** line 139-146:
   ```java
   MetaThought reflection = meta.evaluate(input, rawReply);
   ```

**Impact:** Meta-cognition runs twice. The second evaluation might produce a different result since `ChatSkill` may have already modified `rawReply` with personality.

---

## 5. AgentOrchestrator Analysis

### 5.1 Current Implementation

**File:** `orchestrator/AgentOrchestrator.java`

```java
public String run(String goal, ConversationContext context) throws Exception {
    planner.act(goal, context);
    executor.act(context);
    return reviewer.act(context);
}
```

### 5.2 Issues

| Issue | Detail |
|-------|--------|
| **Never used** | `AgentOrchestrator` is not used by `AgentService` or `AgentBrain` |
| **No integration** | The orchestrator bypasses all skills, memory recall, and personality |
| **PlannerAgent/ExecutorAgent/ReviewerAgent** | These agents have their own independent execution paths |
| **No error handling** | If any agent throws, the entire pipeline fails |

### 5.3 Parallel vs Sequential

The orchestrator runs **planner → executor → reviewer** sequentially. There is no:
- Parallel execution
- Debate between agents
- Result reconciliation
- Confidence-based agent selection

---

## 6. Autonomous Loop Integration

### 6.1 How AutonomousLoop Connects

```
AutonomousEngine.think() [every 5s]
  → contextStore.getContext()
  → AutonomousLoop.run(context)
    → Goals exist? Yes → Execute sub-goal via ChatSkill
    → No → Return null (do nothing)
```

### 6.2 Issues

| Issue | Detail |
|-------|--------|
| **Uses ChatSkill directly** | AutonomousLoop bypasses AgentBrain entirely |
| **No governor check** | Autonomous steps skip the safety governor |
| **No state machine** | Autonomous steps don't go through ConversationStateMachine |
| **Fixed context** | Uses `contextStore.getContext()` which may be stale |

---

## 7. Execution Path Redundancy Map

```
User Input → AgentService → AgentBrain → ChatSkill
                  │              │            │
                  │              │            ├── Recall (duplicate #1)
                  │              │            ├── Meta-cognition (duplicate #2)
                  │              │            └── Personality (duplicate #3)
                  │              │
                  │              ├── Recall (duplicate #1)
                  │              ├── Meta-cognition (duplicate #2)
                  │              └── Personality (duplicate #3)
                  │
Autonomous: AutonomousEngine → AutonomousLoop → ChatSkill (bypasses everything)
Orchestrator: AgentOrchestrator → PlannerAgent → ExecutorAgent → ReviewerAgent (unused)
```

---

## 8. Recommendations

1. **Remove duplicate personality application**: Personality should be applied only once, ideally as the final step in AgentBrain
2. **Move memory recall to single point**: Perform recall once in AgentBrain and pass it to ChatSkill via context
3. **Remove duplicate meta-cognition**: Let ChatSkill handle its own meta-evaluation, or let AgentBrain handle it centrally
4. **Integrate AgentOrchestrator**: Either use it or remove it to avoid confusion
5. **Route autonomous loop through AgentBrain**: Ensure governor and state machine checks for autonomous steps
6. **Add CONTINUE intent**: Detect "next", "continue", "resume" for lesson progression
7. **Add LEARN intent**: Detect "learn" for proper study routing instead of DEFAULT

---

## 9. Summary

| Requirement | Status | Details |
|-------------|--------|---------|
| Correct skill routing | ✅ Works | SkillRouter correctly maps intents to skills |
| Correct planner usage | ❌ Orphan | AgentOrchestrator exists but is never called |
| Correct memory usage | ❌ Duplicate | Memory recall executed twice in same pipeline |
| No duplicate execution paths | ❌ Failed | 3 operations duplicated (recall, meta, personality) |
| Governor in all paths | ❌ Failed | AutonomousLoop bypasses governor |
| State machine in all paths | ❌ Failed | AutonomousLoop bypasses state machine |