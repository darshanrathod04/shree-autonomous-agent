package com.darshan.agent.memory;

public class MemoryVector {

    private final String text;
    private final double[] embedding;

    public MemoryVector(String text, double[] embedding) {
        this.text = text;
        this.embedding = embedding;
    }

    public String getText() {
        return text;
    }

    public double[] getEmbedding() {
        return embedding;
    }
}
