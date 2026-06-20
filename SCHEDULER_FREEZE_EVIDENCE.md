# Scheduler Freeze Investigation - Evidence

## Root Cause Analysis

### 1. Thread Model
- `AutonomousScheduler` uses Spring's `@Scheduled(fixedDelay = 15000)` which runs on a **single-threaded** task executor (`ThreadPoolTaskScheduler` with pool size 1 by default)
- Only ONE scheduler thread exists: `scheduling-1`
- When this thread blocks on `OllamaClient.generateDirect()`, NO other scheduler ticks can run
- But the thread IS blocked, so the `paused` flag is never checked mid-flight

### 2. generateDirect() Blocking Behavior
- `OkHttpClient.newCall(request).execute()` is a **synchronous blocking call**
- It blocks the calling thread for the entire duration of the HTTP request
- With `readTimeout=180s`, a single thought can block the scheduler thread for up to 3 minutes
- During this time, the scheduler cannot respond to `paused` flag changes

### 3. Shared Ollama Instance
- `OllamaClient` is a Spring `@Component` singleton
- Both scheduler and user requests call the SAME `OllamaClient` instance
- Both use the SAME `OkHttpClient` with the SAME connection pool
- When scheduler is blocked on Ollama, user requests queue up behind it
- This causes user requests to also block or timeout

### 4. The `paused` Flag is Useless Mid-Flight
- `paused` is checked at the START of each scheduler tick
- Once a tick starts and calls `loop.run()` → `skill.execute()` → `generateDirect()`, the thread blocks
- The `paused` flag can be set to `true` by a user request, but the scheduler thread is already blocked on Ollama
- The flag only prevents NEW ticks from starting, not cancels running ones

### 5. Overlap Pattern
- With `fixedDelay=15000` (15s) and thoughts taking 42s-129s:
  - Tick 1 starts at T=0, blocks for 42s
  - Tick 2 would start at T=15s, but thread is still blocked
  - Tick 3 would start at T=30s, still blocked
  - Tick 4 would start at T=45s, still blocked
  - This creates a backlog of missed ticks

## Fixes Applied

### Fix 1: Overlap Detection
**File:** `AutonomousScheduler.java`
- Added `thoughtInProgress` AtomicBoolean flag
- If a new tick fires while previous thought is still running, it's SKIPPED
- Overlap count is tracked and logged
- Thread name set to `shree-autonomous-scheduler` for diagnostics

### Fix 2: Dedicated Scheduler Ollama Client
**File:** `OllamaClient.java`
- Added `schedulerClient` - a separate `OkHttpClient` instance
- Added `generateScheduler()` method that uses the dedicated client
- Added `activeRequestCount` instrumentation
- Added `callerIdentity` ThreadLocal for per-request tracking
- All methods now track START/END with thread name and active request count

### Fix 3: Scheduler Uses Direct Ollama Call
**File:** `AutonomousLoop.java`
- Changed from `skill.execute()` (which goes through full brain pipeline) to direct `ollamaClient.generateScheduler()`
- This avoids competing with user requests for the same Ollama connection pool
- Uses a simpler prompt optimized for autonomous thinking

### Fix 4: User Request Pause is Immediate
**File:** `AgentService.java` (already had this)
- `AutonomousScheduler.pause()` is called at the START of every user request
- `AutonomousScheduler.resume()` is called in the `finally` block
- This prevents NEW scheduler ticks from starting during user interaction

## Instrumentation Added

| Metric | Location | How to Observe |
|--------|----------|---------------|
| Thread name | `AutonomousScheduler.think()` | Logs show `shree-autonomous-scheduler` |
| Overlap count | `AutonomousScheduler.overlapCount` | Logged as WARN when overlap detected |
| Active Ollama requests | `OllamaClient.activeRequestCount` | Logged in every START/END log line |
| Caller identity | `OllamaClient.callerIdentity` | Logged as "scheduler", "user", or "user-brain" |
| Request duration | `OllamaClient` START/END | Every call logs elapsed time |
| Tick counter | `AutonomousScheduler.tickCounter` | Every tick logged with sequence number |

## Expected Behavior After Fixes

1. **No overlap:** If scheduler thought takes 42s, next tick at 15s is skipped (logged as WARN)
2. **No user blocking:** User requests use separate `OkHttpClient` instance
3. **Immediate pause:** User request sets `paused=true`, scheduler skips next tick
4. **No queue buildup:** Missed ticks are skipped, not queued
5. **Full observability:** Every Ollama call logs thread name, caller identity, duration, and active request count