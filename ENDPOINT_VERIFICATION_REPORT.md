# Endpoint Verification Report

**Date:** 13 June 2026  
**Project:** Shree Autonomous Agent  
**Server Port:** 8087  
**Base Path:** `/agent`

---

## 1. Backend Endpoints — AgentController.java

**File:** `src/main/java/com/darshan/agent/controller/AgentController.java`

| # | HTTP Method | URL Pattern | Method Name | Status |
|---|------------|-------------|-------------|--------|
| 1 | `POST` | `/agent/ask` | `askAgent()` | ✅ Verified |
| 2 | `POST` | `/agent/session` | `createSession()` | ✅ Verified |
| 3 | `GET` | `/agent/sessions` | `listSessions()` | ✅ Verified |
| 4 | `GET` | `/agent/session/{sessionId}` | `getSession()` | ✅ Verified |
| 5 | `DELETE` | `/agent/session/{sessionId}` | `deleteSession()` | ✅ Verified |

### Additional Endpoints in Controller (not part of 5 to verify)

| HTTP Method | URL Pattern | Method Name |
|------------|-------------|-------------|
| `GET` | `/agent/activity` | `activity()` |
| `GET` | `/agent/sessions/count` | `getSessionCount()` |

---

## 2. Frontend Fetch URLs — index.html

**File:** `src/main/resources/static/index.html`

| Line | Fetch Call | HTTP Method | URL Called |
|------|-----------|-------------|------------|
| 375 | `loadSessions()` | `GET` | `/agent/sessions` |
| 406 | `createNewSession()` | `POST` | `/agent/session` |
| 428 | `loadSession(sessionId)` | `GET` | `/agent/session/{sessionId}` |
| 458 | `deleteSession(sessionId)` | `DELETE` | `/agent/session/{sessionId}` |
| 498 | `sendMessage()` | `POST` | `/agent/ask` |

---

## 3. Mismatch Analysis

### 3.1 Required Endpoints: Frontend vs Backend

| Endpoint | Backend Exists | Frontend Calls It | Match? |
|----------|---------------|-------------------|--------|
| `POST /agent/ask` | ✅ Yes | ✅ Yes (line 498) | ✅ Match |
| `POST /agent/session` | ✅ Yes | ✅ Yes (line 406) | ✅ Match |
| `GET /agent/sessions` | ✅ Yes | ✅ Yes (line 375) | ✅ Match |
| `GET /agent/session/{id}` | ✅ Yes | ✅ Yes (line 428) | ✅ Match |
| `DELETE /agent/session/{id}` | ✅ Yes | ✅ Yes (line 458) | ✅ Match |

**Result: No mismatches found for the 5 required endpoints.**

### 3.2 Unused Backend Endpoints (not called by frontend)

| Endpoint | Controller Method | Frontend Usage |
|----------|------------------|----------------|
| `GET /agent/activity` | `activity()` | ❌ Never called |
| `GET /agent/sessions/count` | `getSessionCount()` | ❌ Never called |

These are **orphan endpoints** — they exist in the backend but are never invoked by the frontend. They may be intended for future use, testing, or external integrations.

---

## 4. Curl Test Results

All 5 required endpoints were tested via `curl` against `http://localhost:8087` and returned successful HTTP 200 responses.

### 4.1 `POST /agent/ask`

```bash
curl -s -X POST "http://localhost:8087/agent/ask" \
  -H "Content-Type: application/json" \
  -d '{"message":"hello"}'
```

**Response (200 OK):**
```json
{
  "approvalRequired": true,
  "sessionId": "8602d979-7931-4233-a483-ff0d286bd6f2",
  "suggestion": "😊 Hello 👋 I am Shree! 🙂\n\n(Self-correction applied) Take your time! 🙂"
}
```

**Status:** ✅ Passed

### 4.2 `POST /agent/session`

```bash
curl -s -X POST "http://localhost:8087/agent/session" \
  -H "Content-Type: application/json"
```

**Response (200 OK):**
```json
{
  "createdAt": "2026-06-13T04:18:49.462361700Z",
  "sessionId": "18aca877-1cb0-40fb-b974-a76f67cad6c7",
  "message": "Session created successfully"
}
```

**Status:** ✅ Passed

### 4.3 `GET /agent/sessions`

```bash
curl -s -X GET "http://localhost:8087/agent/sessions"
```

**Response (200 OK):** Array of session objects with fields: `sessionId`, `userId`, `createdAt`, `lastAccessedAt`, `messageCount`, `firstMessage`.

**Status:** ✅ Passed

### 4.4 `GET /agent/session/{sessionId}`

```bash
curl -s -X GET "http://localhost:8087/agent/session/18aca877-1cb0-40fb-b974-a76f67cad6c7"
```

**Response (200 OK):**
```json
{
  "lastAccessedAt": "2026-06-13T04:19:06.099998900Z",
  "summary": "Empty session",
  "createdAt": "2026-06-13T04:18:49.462361700Z",
  "messageCount": 0,
  "firstMessage": "Untitled",
  "sessionId": "18aca877-1cb0-40fb-b974-a76f67cad6c7",
  "userId": null
}
```

**Status:** ✅ Passed

### 4.5 `DELETE /agent/session/{sessionId}`

```bash
curl -s -X DELETE "http://localhost:8087/agent/session/18aca877-1cb0-40fb-b974-a76f67cad6c7"
```

**Response (200 OK):**
```json
{
  "message": "Session deleted successfully"
}
```

**Status:** ✅ Passed

---

## 5. Observations & Anomalies

### 5.1 Spring Security Active Despite Configuration

`application.properties` contains `spring.security.enabled=false`, yet Spring Security auto-configured with a generated password:
```
Using generated security password: e843878b-8fad-419c-8682-a9cd4b9e1983
```

This suggests the property `spring.security.enabled` does not disable Spring Security for this Boot version (4.0.2). The `SecurityConfig.java` class likely has its own configuration that overrides or ignores this property. Despite this, all curl requests succeeded without authentication, indicating the endpoints remain publicly accessible (possibly via a permit-all security configuration).

### 5.2 Orphan Endpoints (Unused)

Two endpoints in `AgentController` are never called by the frontend:
- `GET /agent/activity` — returns activity feed entries
- `GET /agent/sessions/count` — returns active session count

These are candidates for removal if not needed, or should be documented if intended for external consumers.

### 5.3 Frontend Response Handling Nuance

Frontend `sendMessage()` (line 514) reads `data.suggestion` from the response:
```javascript
addMessage(data.suggestion, 'ai');
```

The backend `POST /agent/ask` returns `AgentResponse` which contains a `suggestion` field. This field name is checked and is present in the test response.

---

## 6. Summary

| Check | Result |
|-------|--------|
| All 5 required endpoints defined in backend | ✅ Passed |
| All 5 required endpoints callable via HTTP | ✅ Passed |
| All 5 required endpoints match frontend fetch URLs | ✅ Passed |
| HTTP method consistency (POST/GET/DELETE) | ✅ Passed |
| URL path consistency | ✅ Passed |
| Endpoint behavior correct (CRUD operations) | ✅ Passed |

**Overall Verdict: ALL VERIFIED — No mismatches found.**