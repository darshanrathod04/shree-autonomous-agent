package com.darshan.agent.agents;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.AgentCommunicationMemory;

public abstract class BaseAgent {

    protected final AgentCommunicationMemory memory;

    protected BaseAgent(AgentCommunicationMemory memory) {
        this.memory = memory;
    }

    protected void say(String from,
                       String to,
                       String msg) {

        memory.send(from, to, msg);
    }
}
