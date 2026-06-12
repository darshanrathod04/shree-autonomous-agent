package com.darshan.agent.memory;

public class AgentMessage {

    private String fromAgent;
    private String toAgent;
    private String content;
    private long timestamp;

    public AgentMessage(String fromAgent,
                        String toAgent,
                        String content) {

        this.fromAgent = fromAgent;
        this.toAgent = toAgent;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getFromAgent() { return fromAgent; }
    public String getToAgent() { return toAgent; }
    public String getContent() { return content; }
    public long getTimestamp() { return timestamp; }
}
