package com.darshan.agent.memory.semantic;

import java.time.LocalDateTime;

public class Concept {

    private String name;
    private int frequency;
    private LocalDateTime lastSeen;

    public Concept(String name) {
        this.name = name.toLowerCase();
        this.frequency = 1;
        this.lastSeen = LocalDateTime.now();
    }

    // Constructor with frequency for JSON deserialization
    public Concept(String name, int frequency) {
        this.name = name.toLowerCase();
        this.frequency = frequency;
        this.lastSeen = LocalDateTime.now();
    }

    // Default constructor for JSON deserialization
    public Concept() {
    }

    public void reinforce() {
        frequency++;
        lastSeen = LocalDateTime.now();
    }

    public String getName() {
        return name;
    }

    public int getFrequency() {
        return frequency;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }
}