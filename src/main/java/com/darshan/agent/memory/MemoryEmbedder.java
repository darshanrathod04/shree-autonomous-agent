package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

@Component
public class MemoryEmbedder {

    public double[] embed(String text) {

        double[] vector = new double[10];

        for (int i = 0; i < text.length(); i++) {
            vector[i % 10] += text.charAt(i);
        }

        return vector;
    }
}
