package com.darshan.agent.brain;

import org.springframework.stereotype.Component;

@Component
public class IntentEngine {

    public String detectIntent(String input) {

        String text = input.toLowerCase();

        if (text.contains("hello") || text.contains("hi"))
            return "GREETING";

        if (text.contains("weather"))
            return "WEATHER";

        if(input.toLowerCase().contains("summary"))
            return "SUMMARY";

        if (text.contains("study"))
            return "STUDY";
        if (input.toLowerCase().contains("remind")
                || input.toLowerCase().contains("reminder")) {
            return "REMINDER";
        }



        return "DEFAULT";
    }
}
