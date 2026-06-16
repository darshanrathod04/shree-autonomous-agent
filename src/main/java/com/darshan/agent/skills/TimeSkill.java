package com.darshan.agent.skills;

import com.darshan.agent.context.ConversationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.ZoneId;

@Component
public class TimeSkill implements Skill {

    @Override
    public boolean supports(String intent) {
        return intent.equals("TIME");
    }

    @Override
    public String execute(String input, ConversationContext context) {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Calcutta"));
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy, h:mm a");
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("h:mm a");

        String fullTime = now.format(fullFormatter);
        String shortTime = now.format(shortFormatter);

        return "🕐 The current time is **" + fullTime + "** (IST).\n\n"
                + "Is there something specific you'd like to schedule or know about?";
    }
}