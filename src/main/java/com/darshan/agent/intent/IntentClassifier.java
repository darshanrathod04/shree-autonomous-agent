package com.darshan.agent.intent;

import org.springframework.stereotype.Component;

@Component
public class IntentClassifier {

    public IntentType classify(String input) {

        String text = input.toLowerCase();

        if (text.contains("hello") || text.contains("hi")) {
            return IntentType.GREETING;
        }

        if (text.contains("weather")) {
            return IntentType.WEATHER;
        }

        if (text.contains("time")) {
            return IntentType.TIME;
        }

        if (
                text.contains("what") ||
                        text.contains("how") ||
                        text.contains("why") ||
                        text.contains("can") ||
                        text.contains("should") ||
                        text.contains("tell") ||
                        text.contains("?")
        ) {
            return IntentType.CHAT;
        }


        return IntentType.UNKNOWN;
    }
}
