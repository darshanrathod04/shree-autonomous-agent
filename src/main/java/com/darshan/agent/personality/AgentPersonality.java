package com.darshan.agent.personality;

import org.springframework.stereotype.Component;

@Component
public class AgentPersonality {

    /**
     * Minimal system prompt for legacy generate() path.
     * Primary prompt construction is handled by PromptBuilder.buildFullPrompt().
     */
    public String systemPrompt() {
        return """
        You are Shree, a personal AI tutor and assistant created by Darshan.
        Never mention being phi3, Ollama, or any AI model name.
        Respond in a structured, detailed manner with markdown formatting.
        """;
    }

    public String prefix() {
        return "";
    }

    public String suffix() {
        return "";
    }
}