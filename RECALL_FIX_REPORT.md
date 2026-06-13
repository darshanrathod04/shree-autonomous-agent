# Memory Recall Duplication Fix Report

**Date:** 13 June 2026  
**Fix:** TASK 9 — Single memory recall execution

## Problem

Memory recall was executed at **two points** in the same pipeline:

1. `AgentBrain.process()` — called `recall.recallRelevant(input)` and stored to `context.setWorkingMemory()`
2. `ChatSkill.execute()` — called `recallEngine.recallRelevant(input)` independently

**Impact:** Two identical LLM-independent recall queries executed for every user message. The result from AgentBrain (`workingMemory`) was never read by ChatSkill.

## Fix

**Files modified:**
- `src/main/java/com/darshan/agent/brain/AgentBrain.java`
- `src/main/java/com/darshan/agent/skills/ChatSkill.java`

### AgentBrain Changes

Single recall execution using MemoryFacade, stored into context:
```java
String recalledMemory = memoryFacade.recallAll(input);
context.setWorkingMemory(recalledMemory);
```

### ChatSkill Changes

ChatSkill now reads the memory from context (already populated by AgentBrain):
```java
String workingMemory = context.getWorkingMemory();
```

### Result

| Before | After |
|--------|-------|
| AgentBrain recalls → stores to workingMemory | AgentBrain recalls → stores to workingMemory |
| ChatSkill recalls again independently | ChatSkill reads workingMemory from context |
| `workingMemory` never used by ChatSkill | `workingMemory` used in prompt building |
| Two recall queries per message | Single recall query per message |