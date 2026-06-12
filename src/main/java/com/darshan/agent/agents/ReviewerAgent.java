package com.darshan.agent.agents;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.AgentCommunicationMemory;
import okhttp3.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Component
public final class ReviewerAgent extends BaseAgent {

    public ReviewerAgent(AgentCommunicationMemory memory) {
        super(memory);
    }

    public String act(ConversationContext ctx) {

        var inbox = memory.inbox("REVIEWER");

        if (inbox.isEmpty())
            return "Nothing to review.";

        String result =
                inbox.get(inbox.size()-1)
                        .getContent();

        return "Review OK ✅ : " + result;
    }
}
