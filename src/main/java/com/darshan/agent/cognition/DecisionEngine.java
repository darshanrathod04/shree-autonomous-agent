package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class DecisionEngine {

    public String decide(Thought thought) {

        return thought.getAction();
    }
}
