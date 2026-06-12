package com.darshan.agent.skills;

import com.darshan.agent.context.ConversationContext;

public interface Skill {



    boolean supports(String intent);


    String execute(String input, ConversationContext context) throws Exception;
}
