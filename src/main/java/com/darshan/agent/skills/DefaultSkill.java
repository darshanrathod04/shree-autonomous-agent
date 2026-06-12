package com.darshan.agent.skills;

import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.MemoryStore;
import org.springframework.stereotype.Component;

@Component
public class DefaultSkill implements Skill {

    private final OllamaClient llm;


    public DefaultSkill(OllamaClient llm) {
        this.llm = llm;
    }

    @Override
    public boolean supports(String intent) {
        return "DEFAULT".equals(intent);
    }



    @Override
    public String execute(String input,
                          ConversationContext context) {

        return llm.generate(input);
    }

}



