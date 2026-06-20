# REAL FIXES APPLIED

## Fix 1: Session-Isolated Identity Storage

**File Modified:** `src/main/java/com/darshan/agent/brain/perception/IdentityPerceptionEngine.java`

**Changes:**
- Added `perceive(String input, ConversationContext context)` overload
- Now stores user name in per-session `context.setUserName(name)` in addition to global profile
- Added `getGlobalUserName()` method for fallback
- Original `perceive(String input)` marked `@Deprecated`

**Why this works:** Each session has its own `ConversationContext` with isolated `userName`. Session A's identity NEVER leaks to Session B.

## Fix 2: Explicit WHO_AM_I Handler

**File Modified:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`

**Changes:**
- Added `case "WHO_AM_I"` in the intent switch statement (line ~130)
- Reads from per-session `context.getUserName()` first
- Falls back to `identityPerceptionEngine.getGlobalUserName()` if not set in session
- Returns "I don't know your name yet" if no name found

**Why this works:** The intent is now handled before falling through to the LLM, and reads from session-isolated context.

## Fix 3: Increased LLM Timeouts + Full Stack Traces

**File Modified:** `src/main/java/com/darshan/agent/llm/OllamaClient.java`

**Changes:**
- Read timeout: 90s → 180s
- Connect timeout: 10s → 15s  
- Write timeout: 10s → 15s
- Added stack trace logging: `log.error("...", e.getMessage(), e)`

## Fix 4: Duplicate Message Prevention

**File Modified:** `frontend/src/shared/stores/chatStore.ts`

**Changes:**
- Modified `finishStreaming()` to check if last message is already identical assistant response
- Skips adding duplicate if content matches

## Fix 5: Session Switching Clear

**File Modified:** `frontend/src/features/chat/ChatArea.tsx`

**Changes:**
- Added `clearMessages()` call immediately when `sessionId` changes
- Reset `loadedSessionRef` to force fresh load
- Prevents old session messages from showing during load

## Fix 6: PLAN Intent for Roadmap Generation

**File Modified:** `src/main/java/com/darshan/agent/brain/IntentEngine.java`

**Changes:**
- Added "PLAN" intent detection for career/goal inputs

**File Modified:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`

**Changes:**
- Added `case "PLAN"` to generate structured roadmap via `AutonomousPlanningEngine`

## Summary of All Files Changed

| File | Fix |
|------|-----|
| `IdentityPerceptionEngine.java` | Session-isolated identity storage |
| `AgentBrain.java` | WHO_AM_I handler + PLAN handler + ExecutionPlan import |
| `OllamaClient.java` | Timeout increase + stack trace logging |
| `ConversationSession.java` | Added sessionUserName field |
| `chatStore.ts` | Duplicate message prevention |
| `ChatArea.tsx` | Session switching clear |
| `IntentEngine.java` | PLAN intent detection |

**Note:** Backend restart required for all Java changes.