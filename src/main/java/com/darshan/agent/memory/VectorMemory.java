package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

@Component
public class VectorMemory {

    public void store(String text) {
        System.out.println("🧠 Vector stored: " + text);
    }
}
