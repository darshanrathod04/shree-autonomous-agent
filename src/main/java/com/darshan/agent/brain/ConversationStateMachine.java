package com.darshan.agent.brain;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationState;
import org.springframework.stereotype.Component;

@Component
public class ConversationStateMachine {

    public String handle(String input,
                         ConversationContext context) {

        switch (context.getState()) {

            case WAITING_REMINDER_TEXT:
                context.put("reminder_text", input);
                context.setState(ConversationState.WAITING_REMINDER_TIME);
                return "When should I remind you?";

            case WAITING_REMINDER_TIME:
                String text = context.get("reminder_text", String.class);
                context.clear();
                return "Reminder set: " + text + " at " + input;

            default:
                return null;
        }

    }
}
