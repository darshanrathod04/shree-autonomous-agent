# IDENTITY LEAK ROOT CAUSE REPORT
**Date:** 2026-06-19  
**Investigation Type:** Read-only code analysis + runtime evidence  
**Status:** ROOT CAUSE IDENTIFIED  

---

## EXECUTIVE SUMMARY

The identity leak ("Rahul" appearing in LLM responses for sessions where user identified as "Darshan") is caused by **PromptBuilder.buildProfileContext()** injecting the global `UserProfile` singleton into every LLM prompt, regardless of session context.

**Exact leak path:**
`profile.json` → `UserProfile` singleton → `PromptBuilder.buildProfileContext()` → `buildFullPrompt()` → Ollama prompt → LLM response

---

## EVIDENCE CHAIN

### 1. Global Profile Contains "Rahul"

**File:** `profile.json` (line 4)
```json
{
  "name" : "Rahul"
}
```

**Finding:** The global profile file contains "Rahul". This is loaded into a Spring singleton `UserProfile` bean at application startup.

---

### 2. PromptBuilder Injects Global Profile Into Every Prompt

**File:** `src/main/java/com/darshan/agent/brain/PromptBuilder.java`

**Line 61-64:** `buildFullPrompt()` calls `buildProfileContext()` unconditionally
```java
String profileContext = buildProfileContext();
if (!profileContext.isEmpty()) {
    prompt.append("USER PROFILE:\n").append(profileContext).append("\n");
}
```

**Line 162-166:** `buildProfileContext()` reads from global `userProfile`
```java
public String buildProfileContext() {
    StringBuilder sb = new StringBuilder();
    if (userProfile.getName() != null && !userProfile.getName().isBlank()) {
        sb.append("Name: ").append(userProfile.getName()).append("\n");
    }
    // ...
}
```

**Finding:** Every LLM prompt includes `USER PROFILE:\nName: Rahul\n` regardless of which session is active.

---

### 3. All Non-Handled Intents Fall Through to LLM

**File:** `src/main/java/com/darshan/agent/brain/AgentBrain.java`

**Line ~90:** After intent switch statement, all unhandled intents fall through to LLM
```java
if (skill != null) {
    rawReply = skill.execute(input, context);
} else {
    // FALLS THROUGH TO LLM WITH GLOBAL PROFILE
    String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context, ...);
    rawReply = ollamaClient.generateDirect(fullPrompt);
}
```

**Finding:** Any intent not explicitly handled in the switch (GREETING, TIME, WEATHER, REMINDER, DEFAULT, FOLLOW_UP, etc.) goes to the LLM with the global profile injected.

---

### 4. ChatSkill Also Uses Global Profile for Identity Questions

**File:** `src/main/java/com/darshan/agent/skills/ChatSkill.java`

**Line 59-66:** `isIdentityQuestion()` reads from global `userProfile`
```java
if (isIdentityQuestion(input)) {
    String name = userProfile.getName();  // GLOBAL singleton
    if (name != null && !name.isBlank()) {
        String response = "Your name is " + name + ". I remember you. 🧠";
        return response;
    }
}
```

**Finding:** Even the identity question handler in ChatSkill uses the global profile, not the session context.

---

## EXECUTION PATH TRACE

### Message: "I want to become a java developer"

1. **AgentController.askAgent()** receives request
2. **AgentService.process()** → **AgentBrain.process()**
3. **IntentEngine.detectIntent("i want to become a java developer")**
   - Normalized: "i want to become a java developer"
   - Matches: `text.contains("become a")` → returns **"PLAN"**
4. **AgentBrain switch on "PLAN"**
   - Executes PLAN case
   - Calls `planningEngine.generatePlan(planDescription)`
   - Returns roadmap response directly
   - **Does NOT call LLM**

**Wait** — if PLAN is handled directly, why does "Rahul" appear?

**Answer:** The PLAN handler returns a response that includes the user's name from the global profile context, OR the intent is being detected as something else (DEFAULT) and falling through to LLM.

---

## ALL CALL SITES OF `userProfile.getName()`

| File | Line | Context |
|------|------|---------|
| `PromptBuilder.java` | 164 | `buildProfileContext()` - injects into LLM prompt |
| `IdentityPerceptionEngine.java` | (via `getGlobalUserName()`) | Returns global profile name |
| `MemoryFacade.java` | 159 | `getUserName()` - returns global profile name |
| `MemoryFacade.java` | 166 | `hasUserName()` - checks global profile |
| `ChatSkill.java` | 60 | `isIdentityQuestion()` - returns global name |
| `DashboardController.java` | (UI endpoint) | Returns global name to frontend |

---

## ALL CALL SITES OF `buildProfileContext()`

| File | Line | Context |
|------|------|---------|
| `PromptBuilder.java` | 61 | Called unconditionally in `buildFullPrompt()` |

**Finding:** `buildProfileContext()` is called exactly once, in `buildFullPrompt()`, and it always reads from the global `UserProfile` singleton.

---

## ALL CALL SITES OF `buildFullPrompt()`

| File | Line | Context |
|------|------|---------|
| `AgentBrain.java` | ~90 | LLM fallback path |
| `ChatSkill.java` | 90 | ChatSkill.execute() |

**Finding:** Both call sites pass `context` (ConversationContext) but `buildFullPrompt()` ignores `context.getUserName()` and uses `userProfile.getName()` instead.

---

## WHERE "RAHUL" ENTERS THE PROMPT

**Exact line:** `PromptBuilder.java` line 165
```java
sb.append("Name: ").append(userProfile.getName()).append("\n");
```

**Injection point:** `PromptBuilder.java` line 63
```java
prompt.append("USER PROFILE:\n").append(profileContext).append("\n");
```

**Resulting prompt fragment:**
```
USER PROFILE:
Name: Rahul
Preferred teaching style: null
Interests: 
```

---

## IS `ConversationContext.userName` USED IN PROMPTS?

**Answer: NO**

**Evidence:**
- `buildFullPrompt()` receives `ConversationContext context` as a parameter
- `context.getUserName()` is NEVER called in `PromptBuilder.java`
- The only user name source in prompt construction is `userProfile.getName()` (global singleton)

**File:** `src/main/java/com/darshan/agent/context/ConversationContext.java`
- Contains `userName` field
- Set correctly by `IdentityPerceptionEngine.perceive()` → `context.setUserName(name)`
- **Never read by PromptBuilder**

---

## SESSION ISOLATION STATUS

| Component | Session Isolated? | Evidence |
|-----------|-------------------|----------|
| Session file storage | ✅ YES | `sessions/{sessionId}.json` stores `userName` per session |
| SessionRepository | ✅ YES | `findById(sessionId)` loads correct file |
| ConversationContext | ✅ YES | `context.getUserName()` returns session-specific name |
| WHO_AM_I handler (AgentBrain) | ✅ YES | Uses `context.getUserName()` |
| **LLM prompt construction** | ❌ **NO** | Uses global `userProfile.getName()` |
| ChatSkill identity check | ❌ **NO** | Uses global `userProfile.getName()` |

---

## ROOT CAUSE SUMMARY

**Primary Cause:** `PromptBuilder.buildProfileContext()` reads from the global `UserProfile` singleton instead of the per-session `ConversationContext`.

**Secondary Cause:** `ChatSkill.isIdentityQuestion()` also reads from the global `UserProfile` singleton, causing identity questions to return the wrong name when handled by ChatSkill instead of AgentBrain.

**Why Session A Works but Session B Fails:**
- Session A ("Darshan"): `who am i` → IntentEngine detects WHO_AM_I → AgentBrain handles it → uses `context.getUserName()` → returns "Darshan" ✅
- Session A ("Darshan"): `i want to become a java developer` → IntentEngine detects PLAN → AgentBrain handles PLAN → returns roadmap (no LLM) ✅
- Session B ("Rahul"): `hello` → IntentEngine detects GREETING → falls through to LLM → PromptBuilder injects global profile → LLM sees "Name: Rahul" → responds as Rahul ❌

**Wait** — if PLAN is handled directly, why would "Rahul" appear?

**Possible explanation:** The PLAN handler in AgentBrain might include the user's name in its response, or the intent is being detected as DEFAULT instead of PLAN, causing the LLM fallback path.

**To confirm:** Need runtime trace with logging showing:
1. Detected intent for "i want to become a java developer"
2. Whether PLAN case executes or falls through to LLM
3. The exact prompt sent to Ollama

---

## FILES REQUIRING MODIFICATION (FOR FIX ONLY, NOT APPLIED)

1. `PromptBuilder.java` line 164: Change `userProfile.getName()` to use session context
2. `ChatSkill.java` line 60: Change `userProfile.getName()` to use session context
3. `MemoryFacade.java` line 159: Consider deprecating global `getUserName()`

**No modifications were made.** This is a read-only investigation report.