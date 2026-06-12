package com.darshan.agent.router;

import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SkillRouter {

    private final List<Skill> skills;

    public SkillRouter(List<Skill> skills) {
        this.skills = skills;
    }

    public Skill route(String intent) {

        for (Skill skill : skills) {
            if (skill.supports(intent)) {
                return skill;
            }
        }

        // fallback → CHAT
        for (Skill skill : skills) {
            if (skill.supports("CHAT")) {
                return skill;
            }
        }

        throw new RuntimeException("No skill found");
    }
}
