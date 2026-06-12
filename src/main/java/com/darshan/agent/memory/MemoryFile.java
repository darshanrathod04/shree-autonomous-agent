package com.darshan.agent.memory;

import java.util.ArrayList;
import java.util.List;

public class MemoryFile {

    private List<ConversationEntry> history = new ArrayList<>();

    public List<ConversationEntry> getHistory() {
        return history;
    }

    public void setHistory(List<ConversationEntry> history) {
        this.history = history;
    }
}
