package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

@Component
public class UserProfile {

    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}