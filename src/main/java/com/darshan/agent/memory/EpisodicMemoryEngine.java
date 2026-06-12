package com.darshan.agent.memory;

import com.darshan.agent.cognition.MetaThought;
import com.darshan.agent.memory.episodic.Episode;
import com.darshan.agent.memory.episodic.EpisodeStore;
import com.darshan.agent.memory.episodic.EpisodeType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EpisodicMemoryEngine {

    private final EpisodeStore store;
    private final List<Episode> episodes = new ArrayList<>();


    public List<Episode> getEpisodes() {
        return episodes;
    }

    public EpisodicMemoryEngine(EpisodeStore store) {
        this.store = store;
    }

    public void remember(
            String input,
            String response,
            MetaThought meta) {

        EpisodeType type = classify(input);

        Episode episode = new Episode(
                type,
                buildSummary(input, response),
                input,
                response,
                meta != null && meta.isSuccessful() ? 0.9 : 0.4
        );

        store.add(episode);

        System.out.println("🧠 Episode stored: " + episode.getSummary());
    }

    private EpisodeType classify(String input) {

        String text = input.toLowerCase();

        if (text.contains("learn"))
            return EpisodeType.LEARNING;

        if (text.contains("goal"))
            return EpisodeType.GOAL_PROGRESS;

        return EpisodeType.CONVERSATION;
    }

    private String buildSummary(String input, String response) {

        return "User said: " + shorten(input)
                + " → Agent responded.";
    }

    private String shorten(String text) {
        return text.length() > 60
                ? text.substring(0, 60) + "..."
                : text;
    }
    public void store(Episode episode) {
        episodes.add(episode);
    }
    public List<Episode> all() {
        return episodes;
    }
}