package com.darshan.agent.brain.curiosity;

import com.darshan.agent.cognition.MetaThought;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CuriosityEngine {

    private final CuriosityState state = new CuriosityState();
    private final SemanticMemoryEngine semantic;

    public CuriosityEngine(SemanticMemoryEngine semantic) {
        this.semantic = semantic;
    }

    public Optional<String> evaluate(String input,
                                     MetaThought meta) {

        // 1️⃣ failure increases curiosity
        if (!meta.isSuccessful()) {
            state.increase(0.2);
        } else {
            state.decrease(0.05);
        }

        // 2️⃣ unknown concept detection
        String unknown = findUnknownConcept(input);

        if (unknown != null) {
            state.increase(0.3);

            String question =
                    "I want to understand better — what do you mean by \""
                            + unknown + "\"?";

            state.rememberQuestion(question);
            return Optional.of(question);
        }

        // 3️⃣ curiosity threshold
        if (state.level() > 0.7) {

            String question =
                    "Can you tell me more so I can learn deeper about this?";

            state.decrease(0.4);
            state.rememberQuestion(question);

            return Optional.of(question);
        }

        return Optional.empty();
    }

    private String findUnknownConcept(String input) {

        String[] words = input.toLowerCase().split("\\s+");

        for (String w : words) {
            if (w.length() > 4 && !semantic.knows(w)) {
                return w;
            }
        }

        return null;
    }
}