# RUNTIME VERIFICATION REPORT
**Date:** 2026-06-18  
**Mode:** USER-ONLY MODE (scheduler disabled)  
**Backend:** Spring Boot 4.0.2, Java 21  
**Ollama:** phi3:mini, phi3:latest, gpt-oss:120b-cloud  

---

## EXECUTIVE SUMMARY

**Status: FAIL** — Critical identity isolation bug confirmed at runtime.

The session file correctly stores per-session identity (`userName: "Darshan"`), but the LLM response returns the GLOBAL profile name ("Rahul") instead. This proves the WHO_AM_I intent is NOT being caught by the handler — the request falls through to the LLM, which reads the global UserProfile singleton.

---

## RUNTIME EVIDENCE

### Test 1: Session Creation
```
POST /agent/session
Response: {"sessionId":"202ef96f-ecac-4307-9cef-db49f2e51526"}
```

### Test 2: Set Identity
```
POST /agent/ask
Body: {"message":"my name is Darshan","sessionId":"202ef96f-ecac-4307-9cef-db49f2e51526"}
Response: {"suggestion":"😊 Your name is Rahul. I remember you. 🧠 Take your time."}
```

**Expected:** "Your name is Darshan"  
**Actual:** "Your name is Rahul"  
**Verdict: ❌ FAIL**

### Test 3: Verify Session File
```
File: sessions/202ef96f-ecac-4307-9cef-db49f2e51526.json
Content: "userName" : "Darshan"
```

**Finding:** Session file correctly stores "Darshan"  
**Implication:** The per-session storage works. The bug is in the READ path.

### Test 4: WHO_AM_I Intent Detection
```
POST /agent/ask
Body: {"message":"who am i","sessionId":"202ef96f-ecac-4307-9cef-db49f2e51526"}
Response: {"suggestion":"I don't know your name yet. Please tell me your name."}
```

**Expected:** "Your name is Darshan" (from session context)  
**Actual:** "I don't know your name yet"  
**Verdict: ❌ FAIL**

**Root Cause Identified:** The WHO_AM_I intent handler in AgentBrain.java is NOT being triggered. The request falls through to the LLM pipeline, which:
1. Does NOT read from session context
2. Reads from global UserProfile singleton (which contains "Rahul" from a previous test)
3. Generates a response based on global state

---

## ROOT CAUSE ANALYSIS

### Code Path
1. `AgentController.askAgent()` → `AgentService.process()`
2. `AgentService.process()` → `AgentBrain.process()`
3. `AgentBrain.process()` → `intentEngine.detectIntent("who am i")`
4. Expected: returns "WHO_AM_I"
5. Expected: `case "WHO_AM_I"` in switch statement executes
6. Expected: returns `context.getUserName()` = "Darshan"

### Actual Behavior
1. Intent detection returns something OTHER than "WHO_AM_I"
2. Falls through to LLM pipeline
3. LLM reads global UserProfile (contains "Rahul")
4. LLM generates "Your name is Rahul"

### Why Intent Detection Fails
The `IntentEngine.detectIntent()` method checks:
```java
if (text.contains("who am i")
        || text.contains("what is my name")
        || text.contains("mera naam kya")
        || text.equals("whoami")) {
    return "WHO_AM_I";
}
```

The input "who am i" SHOULD match. But the response shows the LLM is being called, which means the intent is NOT being detected as WHO_AM_I.

**Possible causes:**
1. The input is being transformed before reaching IntentEngine (e.g., lowercase conversion issue)
2. The IntentEngine is using a different instance or the code wasn't recompiled
3. There's a different code path being executed

---

## CONFIGURATION CHANGES APPLIED

### 1. Scheduler Disabled
**File:** `src/main/resources/application.properties`
```properties
shree.scheduler.enabled=false
```

**Verified:** Backend log shows `[Autonomous] Scheduler is DISABLED — running in USER-ONLY MODE`

### 2. IdentityPerceptionEngine Fixed
**File:** `src/main/java/com/darshan/agent/brain/perception/IdentityPerceptionEngine.java`
- Removed: `userProfile.setName(name)` (global singleton write)
- Kept: `context.setUserName(name)` (per-session storage)

### 3. AgentBrain WHO_AM_I Handler Fixed
**File:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`
- Removed: Global fallback `identityPerceptionEngine.getGlobalUserName()`
- Kept: Session-isolated `context.getUserName()`

### 4. AutonomousLoop Planning Guard
**File:** `src/main/java/com/darshan/agent/autonomy/AutonomousLoop.java`
- Changed: `goals.clearGoal()` → returns without clearing
- Prevents: Auto-completion of goals without user confirmation

---

## REMAINING BUGS

### Bug 1: WHO_AM_I Intent Not Detected (CRITICAL)
**Status:** UNRESOLVED  
**Evidence:** Runtime test shows LLM responding instead of handler  
**Impact:** Session identity isolation completely broken  
**Next step:** Add debug logging to IntentEngine.detectIntent() to trace execution

### Bug 2: Global UserProfile Still Used by LLM (CRITICAL)
**Status:** UNRESOLVED  
**Evidence:** LLM response contains "Rahul" (global profile name)  
**Impact:** Even if intent detection works, LLM prompt includes global profile  
**Next step:** Remove UserProfile from LLM prompt construction

### Bug 3: Session Cache Not Cleared Between Tests
**Status:** UNRESOLVED  
**Evidence:** Session files from previous tests (286 files) still in cache  
**Impact:** Stale data may contaminate new sessions  
**Next step:** Clear sessions directory before testing

---

## OLLAMA DIAGNOSTICS

### Models Available
```
phi3:mini       (2.2GB, Q4_0, 131072 context)
phi3:latest     (2.2GB, Q4_0, 131072 context)
gpt-oss:120b-cloud (remote, 116.8B parameters)
```

### Response Times
- Simple intent detection: ~2-3 seconds
- Full LLM response: ~5-10 seconds
- No timeout observed during testing

---

## FIXES APPLIED (CODE CHANGES)

1. ✅ `application.properties` — Added `shree.scheduler.enabled=false`
2. ✅ `AutonomousScheduler.java` — Added config flag + early return
3. ✅ `IdentityPerceptionEngine.java` — Removed `userProfile.setName()`
4. ✅ `AgentBrain.java` — Removed global fallback from WHO_AM_I
5. ✅ `AutonomousLoop.java` — Prevented auto-goal completion

---

## FIXES NOT YET APPLIED (REQUIRED)

1. ❌ IntentEngine.detectIntent() — needs debug logging to trace why WHO_AM_I not detected
2. ❌ PromptBuilder — must remove UserProfile from LLM prompts
3. ❌ SessionRepository — needs cache eviction or manual clearing between tests
4. ❌ All session files should be cleared before fresh testing

---

## FINAL VERDICT

**Status: ❌ FAIL**

The system is NOT production-ready. The identity isolation fix was applied to the correct code paths, but runtime testing reveals the WHO_AM_I intent is not being detected, causing the request to fall through to the LLM which reads the global UserProfile singleton.

**Evidence:**
- Session file correctly stores "Darshan"
- LLM response incorrectly returns "Rahul" (from global profile)
- WHO_AM_I handler never executes

**Required action:**
1. Add logging to IntentEngine to trace intent detection
2. Verify the code changes are compiled into the running JAR
3. Remove UserProfile references from LLM prompt construction
4. Clear all session files and re-test from clean state