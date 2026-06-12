package com.darshan.agent.memory;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AgentCommunicationMemory {

    private final List<AgentMessage> messages = new ArrayList<>();

    // write message
    public void send(String from,
                     String to,
                     String content) {

        messages.add(
                new AgentMessage(from, to, content)
        );
    }

    // read messages for agent
    public List<AgentMessage> inbox(String agent) {

        return messages.stream()
                .filter(m ->
                        m.getToAgent().equals(agent)
                                || m.getToAgent().equals("ALL"))
                .collect(Collectors.toList());
    }

    // shared history
    public List<AgentMessage> history() {
        return messages;
    }
}
