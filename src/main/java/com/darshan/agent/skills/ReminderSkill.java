package com.darshan.agent.skills;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ConversationState;
import org.springframework.stereotype.Component;

@Component
public class ReminderSkill implements Skill {

    @Override
    public boolean supports(String intent) {
        return intent.equals("REMINDER");
    }

    @Override
    public String execute(String input, ConversationContext context) {

        // STEP 1 — User just asked reminder
        if (context.getState() == ConversationState.IDLE) {

            context.setState(
                    ConversationState.WAITING_REMINDER_TIME

            );

            context.setLastIntent("REMINDER");

            return "Sure 👍 When should I set the reminder?";
        }

        // STEP 2 — Waiting for time
        if (context.getState()
                == ConversationState.WAITING_REMINDER_TIME
        ) {

            context.put("reminder_time", input);

            context.setState(ConversationState.IDLE);

            return "✅ Reminder set for: " + input;
        }

        return "I didn't understand the reminder request.";
    }
}

