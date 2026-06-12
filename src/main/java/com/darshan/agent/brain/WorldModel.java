package com.darshan.agent.brain;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WorldModel {

    private final Map<String, Object> state = new ConcurrentHashMap<>();

    public void update(String key, Object value) {
        state.put(key, value);
    }

    public Object get(String key) {
        return state.get(key);
    }

    public Map<String, Object> snapshot() {
        return new HashMap<>(state);
    }
}