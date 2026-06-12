package com.darshan.agent.cognition;

import com.darshan.agent.personality.AgentPersonality;
import org.springframework.stereotype.Component;

@Component
public class ResponseComposer {

    private final AgentPersonality personality;

    public ResponseComposer(AgentPersonality personality) {
        this.personality = personality;
    }

    public String compose(String rawResponse) {

        // apply personality tone
        return personality.prefix()
                + rawResponse.trim()
                + personality.suffix();
    }
}
