package com.darshan.agent.memory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class UserProfile {

    private static final String PROFILE_FILE = "profile.json";

    private final ObjectMapper mapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private String name;
    private String teachingStyle = "mentor";
    private String preferredTone = "friendly";
    private final Map<String, Object> preferences = new HashMap<>();

    @PostConstruct
    public void init() {
        load();
    }

    public void setName(String name) {
        this.name = name;
        save();
    }

    public String getName() {
        return name;
    }

    public String getTeachingStyle() {
        return teachingStyle;
    }

    public void setTeachingStyle(String teachingStyle) {
        this.teachingStyle = teachingStyle;
        save();
    }

    public String getPreferredTone() {
        return preferredTone;
    }

    public void setPreferredTone(String preferredTone) {
        this.preferredTone = preferredTone;
        save();
    }

    public Map<String, Object> getPreferences() {
        return preferences;
    }

    public void setPreference(String key, Object value) {
        preferences.put(key, value);
        save();
    }

    public Object getPreference(String key) {
        return preferences.get(key);
    }

    /**
     * Save profile to profile.json.
     */
    public synchronized void save() {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("teachingStyle", teachingStyle);
            data.put("preferredTone", preferredTone);
            data.put("preferences", preferences);
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(PROFILE_FILE), data);
        } catch (IOException e) {
            System.err.println("Failed to save profile: " + e.getMessage());
        }
    }

    /**
     * Load profile from profile.json.
     */
    public synchronized void load() {
        try {
            File file = new File(PROFILE_FILE);
            if (!file.exists()) {
                save(); // create default
                return;
            }
            Map<String, Object> data = mapper.readValue(file,
                    new TypeReference<Map<String, Object>>() {});
            this.name = (String) data.getOrDefault("name", null);
            this.teachingStyle = (String) data.getOrDefault("teachingStyle", "mentor");
            this.preferredTone = (String) data.getOrDefault("preferredTone", "friendly");
            if (data.containsKey("preferences")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> savedPrefs = (Map<String, Object>) data.get("preferences");
                preferences.putAll(savedPrefs);
            }
        } catch (IOException e) {
            System.err.println("Failed to load profile: " + e.getMessage());
        }
    }
}