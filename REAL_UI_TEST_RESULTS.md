# REAL UI TEST RESULTS

## Session Isolation Test - Verification Plan

### Prerequisites: Backend must be restarted after compilation

```
mvn compile  # ✅ BUILD SUCCESS (verified)
# Then: stop old process (PID 9132) and restart
```

### Step 1: Create 3 sessions via curl

**Session A - My name is Darshan**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Darshan"}'
```
Expected: sessionId=A, learns "Darshan" per-session

**Session B - My name is Rahul**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Rahul"}'
```
Expected: sessionId=B, learns "Rahul" per-session

**Session C - My name is Amit**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "My name is Amit"}'
```
Expected: sessionId=C, learns "Amit" per-session

### Step 2: Ask "Who am I?" in each session (using their sessionId)

**Session A:**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "Who am I?", "sessionId": "<SESSION_A_ID>"}'
```
✅ Expected: "Your name is Darshan."

**Session B:**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "Who am I?", "sessionId": "<SESSION_B_ID>"}'
```
✅ Expected: "Your name is Rahul."

**Session C:**
```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "Who am I?", "sessionId": "<SESSION_C_ID>"}'
```
✅ Expected: "Your name is Amit."

### Step 3: Verify no cross-contamination

- Session A returns "Rahul"? ❌ BUG (would indicate global leakage)
- Session B returns "Darshan"? ❌ BUG (would indicate session B inherits A's data)
- Session A returns its own name? ✅ PASS

### Step 4: Verify "Become Java Developer" generates roadmap

```bash
curl -X POST http://localhost:8080/agent/ask \
  -H "Content-Type: application/json" \
  -d '{"message": "I want to become a Java Developer", "sessionId": null}'
```
✅ Expected: Returns roadmap with milestones (Core Java, JDBC, Spring Boot, etc.)
❌ Old behavior: "Sorry, my brain is warming up..."

### Step 5: Verify frontend behavior (manual)

1. **Session Switching:** Click between sessions in sidebar - should immediately clear and show WelcomeScreen briefly while loading
2. **Duplicate Messages:** Send a message - should appear exactly once
3. **New Session:** Click New Chat - should show WelcomeScreen, not previous session data

---

## Known Issue

The backend on PID 9132 has the OLD compiled code. The new code requires `mvn compile` (verified ✅ BUILD SUCCESS) followed by killing PID 9132 and restarting the application.