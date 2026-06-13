# Intent Engine Improvement Report

**Date:** 13 June 2026  
**Fix:** TASK 6 — Added LEARN, CONTINUE, WHO_AM_I intents

## Problem

The `IntentEngine` only detected GREETING, WEATHER, SUMMARY, STUDY, REMINDER, and DEFAULT. Common user commands like "learn X", "next", "continue", and "who am I" all fell through to DEFAULT, preventing proper skill routing.

## Fix

**File modified:** `src/main/java/com/darshan/agent/brain/IntentEngine.java`

### Added Intents

| Intent | Trigger Keywords | Priority |
|--------|-----------------|----------|
| `WHO_AM_I` | "who am i", "what is my name", "mera naam kya", "whoami" | Highest |
| `STUDY` (enhanced) | "learn X", "i want to learn", "teach me", "study" | High |
| `CONTINUE` | "next", "continue", "resume", "go on", "keep going", "next step", "next topic" | Medium |

### Detection Order

```
1. WHO_AM_I  (highest priority — identity questions)
2. STUDY     (learning/teaching intent)
3. CONTINUE  (lesson progression)
4. GREETING  (greeting)
5. WEATHER   (weather queries)
6. SUMMARY   (summarization)
7. REMINDER  (reminder setting)
8. DEFAULT   (fallback)
```

### Result

| User Input | Before | After |
|-----------|--------|-------|
| "who am i" | DEFAULT | WHO_AM_I |
| "Learn Spring Boot" | DEFAULT | STUDY |
| "next" | DEFAULT | CONTINUE |
| "continue" | DEFAULT | CONTINUE |