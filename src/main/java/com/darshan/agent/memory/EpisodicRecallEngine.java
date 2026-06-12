package com.darshan.agent.memory;

import com.darshan.agent.memory.episodic.Episode;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EpisodicRecallEngine {

    private final EpisodicMemoryEngine memory;

    public EpisodicRecallEngine(EpisodicMemoryEngine memory) {
        this.memory = memory;
    }

    public String recall(String text) {
        return recallRelevant(text);
    }

    /**
     * Recall relevant past experiences for planning.
     */
    public String recallRelevant(String goalText) {

        List<Episode> episodes = memory.all(); // ⭐ IMPORTANT

        if (episodes.isEmpty()) {
            return "";
        }

        String keyword = goalText.toLowerCase();

        List<Episode> relevant =
                episodes.stream()
                        .filter(e ->
                                e.getUserInput().toLowerCase().contains(keyword)
                                        || e.getAgentResponse().toLowerCase().contains(keyword)
                        )
                        .sorted(Comparator.comparing(Episode::getTimestamp).reversed())
                        .limit(5)
                        .collect(Collectors.toList());

        if (relevant.isEmpty()) {
            return "";
        }

        StringBuilder summary = new StringBuilder();

        summary.append("Relevant past experiences:\n");

        for (Episode e : relevant) {
            summary.append("- User: ")
                    .append(e.getUserInput())
                    .append("\n  Outcome: ")
                    .append(e.getAgentResponse())
                    .append("\n");
        }

        return summary.toString();
    }
}