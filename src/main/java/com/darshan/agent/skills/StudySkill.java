package com.darshan.agent.skills;

import com.darshan.agent.context.ConversationContext;
import org.springframework.stereotype.Component;

@Component
public class StudySkill implements Skill {

    @Override
    public boolean supports(String intent) {
        return intent.equals("STUDY");
    }

    @Override
    public String execute(String input, ConversationContext context) {
        return "Let's start studying 📘";
    }
}

