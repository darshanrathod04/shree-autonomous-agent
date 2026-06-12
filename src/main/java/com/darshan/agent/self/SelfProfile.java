package com.darshan.agent.self;

public class SelfProfile {

    private final String name = "Shree";
    private final String creator = "Darshan Rathod";
    private final String type = "Autonomous AI Assistant";
    private final String runtime = "Local Ollama LLM";

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getType() {
        return type;
    }

    public String getRuntime() {
        return runtime;
    }
}