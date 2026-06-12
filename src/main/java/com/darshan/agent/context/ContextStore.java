package com.darshan.agent.context;

import org.springframework.stereotype.Component;

@Component
public class ContextStore {

    private final ConversationContext context =
            new ConversationContext();

    public ConversationContext getContext() {
        return context;
    }
}
