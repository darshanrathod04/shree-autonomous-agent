package com.darshan.agent.cognition;

import org.springframework.stereotype.Component;

@Component
public class PerceptionEngine {

    public String detectGoal(String input) {

        input = input.toLowerCase();

        if (input.contains("plan")
                || input.contains("schedule")
                || input.contains("roadmap")) {

            return "PLANNING";
        }

        if (input.contains("remind"))
            return "REMINDER";

        return "GENERAL";

    }


}
