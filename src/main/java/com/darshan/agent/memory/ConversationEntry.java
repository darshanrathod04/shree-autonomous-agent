package com.darshan.agent.memory;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConversationEntry {

    private String user;
    private String assistant;
    private String status = "PENDING";

    public ConversationEntry() {}

    public ConversationEntry(String user, String assistant) {
        this.user = user;
        this.assistant = assistant;
    }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getAssistant() { return assistant; }
    public void setAssistant(String assistant) {
        this.assistant = assistant;
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
