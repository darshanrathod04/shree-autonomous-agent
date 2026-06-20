package com.darshan.agent.brain.perception;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityPerceptionEngine {

    private final UserProfile userProfile;
    private final EpisodicMemoryEngine episodicMemory;

    /**
     * Extract and store user identity from input.
     * Stores name in both the per-session context and global profile for persistence.
     * @param input The user's message
     * @param context The per-session conversation context (for session isolation)
     */
    public void perceive(String input, ConversationContext context) {

        String normalized = normalize(input);

        String name = extractName(normalized);

        if (name == null) return;

        // store in per-session context for session-isolated identity
        context.setUserName(name);

        // store as episodic memory (long-term cognition), tagged with session context info
        Episode episode = new Episode(
                EpisodeType.CONVERSATION,
                "Learned user's name: " + name,
                input,
                "Stored identity: " + name,
                1.0
        );

        episodicMemory.store(episode);

        System.out.println("🧠 Identity Learned → " + name + " (session: " + (context.getUserName() != null ? context.getUserName() : "unknown") + ")");
    }

    /**
     * Get the currently stored user name (from global profile).
     * Used as fallback when session context has no name.
     */
    public String getGlobalUserName() {
        return userProfile.getName();
    }

    /**
     * Backward-compatible perceive without context (uses global UserProfile).
     * @deprecated Use perceive(input, context) for session isolation
     */
    @Deprecated
    public void perceive(String input) {
        perceive(input, new ConversationContext());
    }

    private String extractName(String text) {

        Pattern p = Pattern.compile(
                "(?:my name is|i am|mera naam|mai)\\s+([a-zA-Z ]+)"
        );

        Matcher m = p.matcher(text);

        if (m.find()) {
            return capitalize(m.group(1).trim());
        }

        return null;
    }

    private String normalize(String input) {
        return input.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String capitalize(String name){
        return Arrays.stream(name.split(" "))
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1))
                .collect(Collectors.joining(" "));
    }
}