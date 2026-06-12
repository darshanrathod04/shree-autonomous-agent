package com.darshan.agent.agents;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.AgentCommunicationMemory;
import org.springframework.stereotype.Component;

@Component
public class ExecutorAgent extends BaseAgent {

    public ExecutorAgent(AgentCommunicationMemory memory) {
        super(memory);
    }

    public String act(ConversationContext ctx) {

        var inbox = memory.inbox("EXECUTOR");

        if (inbox.isEmpty())
            return "Nothing to execute.";

        String task =
                inbox.get(inbox.size()-1)
                        .getContent();

        String result =
                "Executed: " + task;

        say("EXECUTOR", "REVIEWER", result);

        return result;
    }
}

