# Runtime Behavior Bug Analysis

## Bug 1: "Become Java Developer" returns "Sorry, my brain is warming up"

**Root Cause:** The Ollama LLM call is timing out or failing silently. The `OllamaClient.generateDirect()` method catches all exceptions and returns the fallback message "Sorry, my brain is warming up...". The 90-second read timeout is too aggressive for complex prompts, and there's no retry logic or detailed error logging to distinguish between timeout, connection refused, or model errors.

**Code Path:**
- `AgentService.process()` → `AgentBrain.process()` → `OllamaClient.generateDirect()` → exception → fallback message

## Bug 2: Planning engine not triggering

**Root Cause:** The `AutonomousPlanningEngine` is never invoked for career/goal-oriented inputs. The `IntentEngine` has no "PLAN" or "ROADMAP" intent, so messages like "I want to become a Java Developer" are classified as "DEFAULT" and processed as regular conversation. The planning engine only generates plans when `generatePlan()` is called explicitly, which never happens from user input.

**Code Path:**
- `IntentEngine.detectIntent()` returns "DEFAULT" for goal-oriented messages
- `AgentBrain.process()` skips planning engine for DEFAULT intent
- `AutonomousPlanningEngine.generatePlan()` is never called

## Bug 3: Duplicate assistant messages

**Root Cause:** Double-write pattern. The backend (`AgentService.process()` line 71) adds the AI response to the session history via `sessionManager.addMessage(session, "AI", response.getSuggestion())`. Then the frontend (`ChatInput.tsx` via `finishStreaming()` in chatStore.ts line 137-142) also adds the same assistant message to the chat store when streaming completes. This results in the same message appearing twice.

**Code Path:**
1. Backend: `AgentService.process()` → `sessionManager.addMessage(session, "AI", response)`
2. Frontend: `ChatInput.tsx` → `finishStreaming()` → `addMessage({role: 'assistant', content: response})`

## Bug 4: Session switching shows loading contamination

**Root Cause:** Race condition in `ChatArea.tsx`. When switching sessions:
1. The `sessionId` prop changes
2. `setActiveSession(sessionId)` is called synchronously
3. But `loadMessages()` is async and hasn't completed yet
4. The chatStore still shows old session's messages until new messages load
5. No explicit message clearing before loading new session

Additionally, `loadedSessionRef` is a local ref that resets on component remount, causing unnecessary reloads.

**Code Path:**
- `ChatArea.tsx` useEffect on `sessionId` → async `loadMessages()` → race with UI render

## Bug 5: Roadmap generation is inconsistent

**Root Cause:** The planning engine is not automatically triggered for goal-oriented inputs (same root cause as Bug 2). When users say "I want to become a Java Developer", the system treats it as a conversation instead of generating a structured roadmap. The `AutonomousPlanningEngine.decomposeGoal()` has hardcoded patterns for "java developer" but these are never reached because the intent is not detected.

**Code Path:**
- No PLAN intent → DEFAULT processing → conversational response instead of structured plan
- `AutonomousPlanningEngine.decomposeGoal()` logic exists but is unreachable from user input