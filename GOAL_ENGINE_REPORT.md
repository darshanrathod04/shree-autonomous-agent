# Goal Execution Engine Report

**Date:** 13 June 2026  
**Project:** Shree Autonomous Agent  
**Component:** Autonomous Goal System

---

## 1. Architecture Overview

### 1.1 Goal System Components

| Component | File | Role |
|-----------|------|------|
| `GoalManager` | `autonomy/GoalManager.java` | Manages current goal lifecycle |
| `AgentGoal` | `autonomy/AgentGoal.java` | Goal entity with subgoals |
| `SubGoal` | `autonomy/SubGoal.java` | Individual step within a goal |
| `SubGoalPlanner` | `autonomy/SubGoalPlanner.java` | Generates subgoals for a goal |
| `AutonomousLoop` | `autonomy/AutonomousLoop.java` | Executes goal steps autonomously |
| `AutonomousEngine` | `autonomy/AutonomousEngine.java` | Scheduler (runs loop every 5s) |
| `SelfGoalEngine` | `autonomy/SelfGoalEngine.java` | Creates self-improvement goals on failure |

---

## 2. Goal Creation Flow

### 2.1 User-Initiated Goal

**File:** `service/AgentService.java` (line 44-58)

```java
if (input.toLowerCase().startsWith("goal:")) {
    goals.createGoal(input.replace("goal:", "").trim());
    return new AgentResponse("🎯 Goal accepted. Shree is now working autonomously.", ...);
}
```

`GoalManager.createGoal()`:
```java
public void createGoal(String description) {
    if (currentGoal != null) {
        return; // already working on something — IGNORES new goal!
    }
    currentGoal = new AgentGoal(description);
}
```

**Issue:** If a goal is already active, new goals are silently ignored. No error message or queue.

### 2.2 Self-Initiated Goal

**File:** `autonomy/SelfGoalEngine.java`

```java
public void evaluateForGoal(MetaThought meta) {
    if (goals.hasGoal()) return;       // Already busy
    if (!meta.isSuccessful()) {
        AgentGoal goal = new AgentGoal("Improve response quality and reasoning");
        goals.setGoal(goal);
    }
}
```

Creates a hardcoded self-improvement goal when the agent detects a failure. The goal description is static — never dynamically generated based on the actual failure.

---

## 3. Goal Execution Flow

### 3.1 Autonomous Loop

**File:** `autonomy/AutonomousLoop.java`

```java
public String run(ConversationContext context) throws Exception {
    if (!goals.hasGoal()) return null;  // Nothing to do

    AgentGoal goal = goals.getGoal();

    // Memory recall for goal context
    String memoryContext = recall.recallRelevant(goal.getDescription());
    worldModel.update("episodicContext", memoryContext);

    // Phase 1: Subgoal generation
    if (goal.getSubGoals().isEmpty()) {
        planner.generateSubGoals(goal, memoryContext);
        return "🧠 Plan created.";
    }

    // Phase 2: Execute next pending step
    SubGoal step = goal.nextPending();
    if (step == null) {
        goals.clearGoal();
        return "✅ Goal completed.";
    }

    // Execute via CHAT skill
    Skill skill = router.route("CHAT");
    String result = skill.execute(step.getDescription(), context);

    // Reflection + meta-cognition
    ReflectionResult reflection = reflectionEngine.reflect(...);
    MetaThought meta = metaCognition.observe(thought, reflection);
    motivationEngine.evaluate(result);

    // Step completion detection
    if (isCompleted(result)) {
        step.complete();
        return "✔ Step done: " + step.getDescription();
    }
    return "🧠 Progress: " + result;
}
```

### 3.2 Subgoal Planning

**File:** `autonomy/SubGoalPlanner.java` (generates subgoals from goal description, likely via LLM call)

The planner creates subgoals from the goal description using the conversation context and memory recall.

---

## 4. Critical Issues Found

### 4.1 NO GOAL PERSISTENCE

**File:** `GoalManager.java`

```java
private AgentGoal currentGoal;  // In-memory only — LOST ON RESTART
```

| Data | Persisted? | Survives Restart? |
|------|-----------|-------------------|
| Goal description | ❌ No | ❌ No |
| Subgoal list | ❌ No | ❌ No |
| Subgoal completion status | ❌ No | ❌ No |
| Goal completion history | ❌ No | ❌ No |

### 4.2 Single Goal Limit

```java
public void createGoal(String description) {
    if (currentGoal != null) {
        return; // Silently ignores new goals
    }
```

- No goal queue
- No prioritization
- No way to replace an existing goal
- No feedback to user when goal is ignored

### 4.3 Step Completion Detection is Fragile

**File:** `AutonomousLoop.java` (line 163-172)

```java
private boolean isCompleted(String result) {
    String lower = result.toLowerCase();
    return lower.contains("completed")
        || lower.contains("done")
        || lower.contains("finished")
        || lower.contains("success");
}
```

Keyword-based detection relies on LLM output containing specific words. If the LLM uses different phrasing (e.g., "that's all set", "I've wrapped it up"), the step will never be marked complete.

### 4.4 No Goal History Tracking

Once a goal is cleared (`goals.clearGoal()`), there is no record of:
- What goal was completed
- When it was completed
- What subgoals were accomplished
- How successful the execution was

### 4.5 Loop Frequency

The `AutonomousEngine` runs every 5 seconds:
```java
@Scheduled(fixedDelay = 5000)
public void think() throws Exception {
    loop.run(contextStore.getContext());
}
```

This means the autonomous loop executes even when:
- No goal is set (returns immediately, but still runs)
- The user is mid-conversation
- Previous step execution hasn't finished

---

## 5. Goal Lifecycle

```
User: "goal: Learn DSA"
  → AgentService creates goal
  → AutonomousEngine.think() detects goal
    → AutonomousLoop.run():
      1. SubGoalPlanner generates subgoals
         ["Learn arrays", "Learn linked lists", ...]
      2. Loop picks next pending subgoal
      3. Routes to ChatSkill with subgoal description
      4. LLM generates teaching content
      5. Reflection checks quality
      6. If completed → mark subgoal done
      7. Repeat step 2-6
      8. No more subgoals → clear goal → "Goal completed"
```

---

## 6. Recommendations

1. **Persist GoalManager**: Serialize `currentGoal` + subgoal states to `goals.json`
2. **Implement goal queue**: Allow multiple goals with priority ordering
3. **Improve completion detection**: Add more keywords and confidence scoring
4. **Track goal history**: Store completed goals with timestamps and outcomes
5. **Add goal rejection feedback**: Inform user when a goal cannot be accepted
6. **Reduce loop frequency**: Increase to 10-15 seconds, or make it event-driven
7. **Add `goal:list` command**: Let users see active goals and progress

---

## 7. Summary

| Requirement | Status | Details |
|-------------|--------|---------|
| Create subgoals | ✅ Works | SubGoalPlanner generates subgoals |
| Execute step-by-step | ✅ Works | AutonomousLoop executes pending steps |
| Track progress | ⚠️ Partial | SubGoal.completed flag, but not persisted |
| Mark completed steps | ⚠️ Fragile | Keyword-based detection |
| Goal progress persistence | ❌ Failed | In-memory only — lost on restart |
| Resume after restart | ❌ Failed | No persistence mechanism |
| Goal completion history | ❌ Failed | No history tracked |
| Multiple goals | ❌ Failed | Single goal only, silently ignores new goals |