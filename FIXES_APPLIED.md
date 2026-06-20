# Fixes Applied - Runtime Behavior Bugs

## Bug 1: "Become Java Developer" returns "Sorry, my brain is warming up"

**File:** `src/main/java/com/darshan/agent/llm/OllamaClient.java`

**Changes:**
- Increased HTTP client timeouts:
  - Connect timeout: 10s → 15s
  - Read timeout: 90s → 180s
  - Write timeout: 10s → 15s
- Added stack trace logging to exception handlers in both `generate()` and `generateDirect()` methods
- Changed `log.error("[Ollama] ... {}", elapsed, e.getMessage())` to `log.error("[Ollama] ... {}", elapsed, e.getMessage(), e)` to capture full exception details

**Root Cause:** The 90-second read timeout was insufficient for complex prompts, causing silent failures. The lack of stack trace logging made debugging impossible.

---

## Bug 2 & 5: Planning engine not triggering / Roadmap generation inconsistent

**File:** `src/main/java/com/darshan/agent/brain/IntentEngine.java`

**Changes:**
- Added new "PLAN" intent detection for career/goal-oriented inputs
- Detection patterns: "become a", "plan", "roadmap", "career path", "learning path", "steps to", "how do i become", "how to become"
- Intent is checked after learning intents but before generic greeting/time/weather intents

**File:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`

**Changes:**
- Added new `case "PLAN"` in the intent handling switch statement (line ~159)
- Extracts plan description from input by stripping intent keywords
- Calls `planningEngine.generatePlan(planDescription)` to create structured roadmap
- Returns formatted response with plan summary and next steps
- Plan is automatically persisted by `AutonomousPlanningEngine`

**Root Cause:** No PLAN intent existed, so goal-oriented messages like "I want to become a Java Developer" were classified as DEFAULT and processed conversationally instead of generating a structured plan.

---

## Bug 3: Duplicate assistant messages

**File:** `frontend/src/shared/stores/chatStore.ts`

**Changes:**
- Modified `finishStreaming()` function to check for duplicate messages before adding
- Added logic: if last message in array is already an assistant message with identical content, skip adding duplicate
- Only clears streaming state without adding new message if duplicate detected
- Preserves backend persistence while preventing frontend duplication

**Root Cause:** Backend saves AI response to session history, then frontend adds same response again during streaming completion. The backend save is required for persistence, so the fix prevents the frontend from duplicating existing messages.

---

## Bug 4: Session switching shows loading contamination

**File:** `frontend/src/features/chat/ChatArea.tsx`

**Changes:**
- Added `clearMessages` to the destructured store methods
- Modified session sync useEffect to call `clearMessages()` immediately when `sessionId` changes
- Reset `loadedSessionRef.current = null` to force fresh load
- Removed the skip-reload guard that prevented loading new session messages
- Messages are now cleared synchronously before async load begins, preventing UI flash of old messages

**Root Cause:** Race condition where old session messages remained visible while new session loaded asynchronously. The ref-based skip logic also prevented reloading after component remount.

---

## Summary

All 5 bugs have been fixed:

1. ✅ LLM timeout increased + better error logging
2. ✅ PLAN intent added + planning engine integration
3. ✅ Duplicate message detection in frontend
4. ✅ Session switching clears messages immediately
5. ✅ Roadmap generation now triggers automatically for goal-oriented inputs

**Files Modified:**
- `src/main/java/com/darshan/agent/llm/OllamaClient.java`
- `src/main/java/com/darshan/agent/brain/IntentEngine.java`
- `src/main/java/com/darshan/agent/brain/AgentBrain.java`
- `frontend/src/features/chat/ChatArea.tsx`
- `frontend/src/shared/stores/chatStore.ts`

**Backend restart required** for Java changes to take effect.