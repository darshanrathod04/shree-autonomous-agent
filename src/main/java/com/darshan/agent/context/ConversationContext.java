package com.darshan.agent.context;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ConversationContext {

    @Setter
    @Getter
    private String lastIntent;

    private String mode = "CHAT"; // CHAT | TEACHING
    private String topic = null;

    private String currentTopic;

    private String workingMemory;

    public void setWorkingMemory(String memory){
        this.workingMemory = memory;
    }

    public String getWorkingMemory(){
        return workingMemory;
    }

    public void setCurrentTopic(String topic){
        this.currentTopic = topic;
    }

    public String getCurrentTopic(){
        return currentTopic;
    }



    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return mode;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getTopic() {
        return topic;
    }


    private final List<ConversationEntry> history =
            new ArrayList<>();

    public void addUserMessage(String message) {
        history.add(new ConversationEntry("USER", message));
        trim();
    }

    public void addAgentMessage(String message) {
        history.add(new ConversationEntry("AI", message));
        trim();
    }

    private void trim() {
        if (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    public String getConversationSummary() {

        StringBuilder sb = new StringBuilder();

        for (ConversationEntry entry : history) {

            sb.append(entry.getRole())
                    .append(": ")
                    .append(entry.getMessage())
                    .append("\n");
        }

        return sb.toString();
    }

    private static final int MAX_HISTORY = 10;

    private ConversationState state = ConversationState.IDLE;

    private Map<String, Object> data = new HashMap<>();

    public ConversationState getState() {
        return state;
    }

    public void setState(ConversationState state) {
        this.state = state;
    }

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key) {
        return data.get(key);
    }

    public void clear() {
        data.clear();
        state = ConversationState.IDLE;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        return (T) data.get(key);
    }
    public <T> T getOrDefault(String key, T defaultValue) {
        Object value = data.get(key);
        return value == null ? defaultValue : (T) value;
    }

    private String userName;

    public void setUserName(String name) {
        this.userName = name;
    }

    public String getUserName() {
        return userName;
    }

}

