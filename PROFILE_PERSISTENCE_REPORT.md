# Profile Persistence Report

**Date:** 13 June 2026  
**Fix:** TASK 2 — User profile persistence

## Problem

`UserProfile` was in-memory only. User's name, teaching style, and preferences were lost on restart. `IdentityPerceptionEngine` also stored name as episodic memory, but episodic memory was also in-memory.

## Fix

**File modified:** `src/main/java/com/darshan/agent/memory/UserProfile.java`

### Changes Made

1. **Auto-load on startup** via `@PostConstruct init()`:
   ```java
   @PostConstruct
   public void init() { load(); }
   ```

2. **Auto-save on every change** — `setName()`, `setTeachingStyle()`, `setPreferredTone()`, `setPreference()` all call `save()` after updating.

3. **Persistence format** — `profile.json` using Map serialization (avoids JSR310 dependency issues).

4. **Added fields:**
   - `teachingStyle` — persisted (was hardcoded in PersonalityProfile)
   - `preferredTone` — persisted
   - `preferences` Map — extensible key-value store

### Profile File (profile.json)

```json
{
  "name": "Darshan",
  "teachingStyle": "mentor",
  "preferredTone": "friendly",
  "preferences": {}
}
```

**Result:** User identity survives restart. Name is loaded from `profile.json` on startup.