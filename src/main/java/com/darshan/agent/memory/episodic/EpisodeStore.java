package com.darshan.agent.memory.episodic;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EpisodeStore {

    private final List<Episode> episodes = new ArrayList<>();

    public void add(Episode episode) {
        episodes.add(episode);
    }

    public List<Episode> recent(int limit) {

        return episodes.stream()
                .skip(Math.max(0, episodes.size() - limit))
                .collect(Collectors.toList());
    }

    public List<Episode> all() {
        return episodes;
    }
}