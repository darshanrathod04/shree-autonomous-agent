package com.darshan.agent.brain.perception;

import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeType;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class IdentityPerceptionEngine {

    private final UserProfile userProfile;
    private final EpisodicMemoryEngine episodicMemory;

    public void perceive(String input) {

        String normalized = normalize(input);

        String name = extractName(normalized);

        if (name == null) return;

        // store in profile (short-term identity)
        userProfile.setName(name);

        // store as episodic memory (long-term cognition)
        Episode episode = new Episode(
                EpisodeType.CONVERSATION,
                "Learned user's name: " + name,
                input,
                "Stored identity: " + name,
                1.0
        );

        episodicMemory.store(episode);

        System.out.println("🧠 Identity Learned → " + name);
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