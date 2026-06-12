package com.darshan.agent.agents;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.AgentCommunicationMemory;
import org.springframework.stereotype.Component;

@Component
public class PlannerAgent extends BaseAgent {

    public PlannerAgent(AgentCommunicationMemory memory) {
        super(memory);
    }

    public String act(String goal,
                      ConversationContext ctx) {

        String plan =
                "Plan created for: " + goal;

        say("PLANNER", "EXECUTOR", plan);

        return plan;
    }
}
