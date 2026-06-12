package com.darshan.agent.service;

import com.darshan.agent.context.ContextStore;
import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.brain.AgentBrain;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.dto.AgentResponse;
import org.springframework.stereotype.Service;

@Service
public class AgentService {

    private final AgentBrain brain;
    private final GoalManager goals;
    private final ContextStore contextStore;

    public AgentService(
            AgentBrain brain,
            GoalManager goals,
            ContextStore contextStore) {

        this.brain = brain;
        this.goals = goals;
        this.contextStore = contextStore;
    }

    public AgentResponse process(String input) throws Exception {

        ConversationContext context =
                contextStore.getContext();

        // 🎯 Goal command
        if (input.toLowerCase().startsWith("goal:")) {

            goals.createGoal(
                    input.replace("goal:", "").trim()
            );

            return new AgentResponse(
                    "🎯 Goal accepted. Shree is now working autonomously.",
                    false
            );

        }

        return brain.process(input, context);
    }
}
