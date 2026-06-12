package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

@Component
public class ActivityFeed {

    private final LinkedList<String> activities = new LinkedList<>();

    public synchronized void add(String activity) {

        activities.addFirst(activity);

        // keep last 20 events
        if (activities.size() > 20) {
            activities.removeLast();
        }
    }

    public synchronized List<String> getRecentActivity() {
        return List.copyOf(activities);
    }
}