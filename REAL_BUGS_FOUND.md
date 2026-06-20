# REAL BUGS FOUND

## Critical Bug: Session Isolation Failure via Global Identity

**File:** `src/main/java/com/darshan/agent/brain/perception/IdentityPerceptionEngine.java`

**Root Cause:** The `perceive()` method stored user identity in a global singleton `UserProfile` via `userProfile.setName(name)`. Since `UserProfile` is a Spring `@Component` singleton, ALL sessions shared the same identity state. When Session A said "My name is Darshan", Session B asking "Who am I?" would also return "Darshan".

**Evidence:**
- Line 35: `userProfile.setName(name)` — global singleton storage
- The `ConversationContext` already had `userName` field (line 132-140) but it was NEVER set
- Only the global UserProfile was written to

**Impact:** Zero session isolation for identity. Privacy violation.

---

## Bug: "Who Am I?" Returns Global Name

**File:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`

**Root Cause:** No explicit handler for "WHO_AM_I" intent. The intent was detected but fell through to the LLM which reads the global UserProfile as context.

---

## Bug: LLM Timeout Causing "Brain Warming Up"

**File:** `src/main/java/com/darshan/agent/llm/OllamaClient.java`

**Root Cause:** 90-second read timeout was insufficient for complex prompt responses. Also, exception stack traces were not logged (missing `e` parameter in log.error), making debugging impossible.

---

## Bug: Duplicate Assistant Messages

**Locations:**
- `src/main/java/com/darshan/agent/service/AgentService.java` (backend saves AI response)
- `frontend/src/shared/stores/chatStore.ts` (frontend adds message on streaming complete)

**Root Cause:** Both backend and frontend add the same assistant message to their respective stores, resulting in duplicates when the backend session is loaded.

---

## Bug: Session Switching Contamination

**File:** `frontend/src/features/chat/ChatArea.tsx`

**Root Cause:** When switching sessions, old messages remained visible while async loadMessages() was in progress. No immediate clearMessages() call before loading new session.