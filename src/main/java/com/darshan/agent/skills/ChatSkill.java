package com.darshan.agent.skills;

import com.darshan.agent.brain.BrainInterface;
import com.darshan.agent.brain.PromptBuilder;
import com.darshan.agent.brain.executive.ExecutiveControlEngine;
import com.darshan.agent.brain.executive.ExecutiveDecision;
import com.darshan.agent.cognition.MotivationEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.MemoryFacade;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.self.SelfModelEngine;
import org.springframework.stereotype.Component;

@Component
public class ChatSkill implements Skill {

    private final BrainInterface brain;

    private final MotivationEngine motivationEngine;
    private final ExecutiveControlEngine executive;
    private final SelfModelEngine selfModel;
    private final MemoryFacade memoryFacade;
    private final SemanticMemoryEngine semantic;
    private final PromptBuilder promptBuilder;

    public ChatSkill(
            BrainInterface brain,
            MotivationEngine motivationEngine,
            ExecutiveControlEngine executive,
            SelfModelEngine selfModel,
            MemoryFacade memoryFacade,
            SemanticMemoryEngine semantic,
            PromptBuilder promptBuilder
    ) {
        this.brain = brain;
        this.motivationEngine = motivationEngine;
        this.executive = executive;
        this.selfModel = selfModel;
        this.memoryFacade = memoryFacade;
        this.semantic = semantic;
        this.promptBuilder = promptBuilder;
    }

    @Override
    public boolean supports(String intent) {
        return "CHAT".equals(intent)
                || "DEFAULT".equals(intent)
                || "FOLLOW_UP".equals(intent);
    }

    @Override
    public String execute(String input, ConversationContext context) {
        System.out.println("[ChatSkill] execute() called | input='" + input + "'");

        // HARD IDENTITY RECALL
        if (isIdentityQuestion(input)) {

            String sessionName = context != null
                    ? context.getUserName()
                    : null;

            System.out.println(
                    "[ChatSkill] IDENTITY BRANCH | sessionName="
                            + sessionName);

            if (sessionName != null && !sessionName.isBlank()) {

                String response =
                        "Your name is " + sessionName + ". I remember you. 🧠";

                System.out.println(
                        "[ChatSkill] IDENTITY RESPONSE: "
                                + response);

                return response;
            }

            return "I don't know your name yet. Tell me your name and I'll remember it! 😊";
        }

        if (isCreatorQuestion(input)) {
            return """
I was created by Darshan Rathod.

My purpose is to help users learn,
track goals, remember context,
and act as an AI assistant.
""";
        }

        if (isMemoryRecallQuestion(input)) {
            String memory = memoryFacade.recallAll(input);
            return memory.isEmpty() ? "I don't remember yet." : memory;
        }

        // EXECUTIVE DECISION
        var decision = executive.decide(input, context, null);

        String instruction = switch (decision.getAction()) {
            case ASK_CLARIFICATION -> "Ask clarification before answering.";
            case START_TEACHING -> "Teach step-by-step with clear explanations.";
            case WAIT -> "Do not respond.";
            case CREATE_GOAL -> "Suggest forming a goal.";
            case RESPOND -> "Respond naturally and helpfully.";
        };

        // USE PROMPT BUILDER (single source of prompt creation)
        String fullPrompt = promptBuilder.buildFullPrompt(input, instruction, context);

        // LLM CALL
        String response = brain.think(fullPrompt);

        // Motivation update
        if (response != null && response.length() > 25)
            motivationEngine.getState().increaseConfidence();
        else
            motivationEngine.getState().decreaseConfidence();

        return response;
    }

    private boolean isMemoryRecallQuestion(String input) {
        String t = input.toLowerCase();
        return t.contains("kya bataya")
                || t.contains("what did i say")
                || t.contains("remember what");
    }

    private boolean isIdentityQuestion(String input) {
        String t = normalize(input);
        return t.contains("who am i")
                || t.contains("what is my name")
                || t.contains("mera naam")
                || t.contains("naam kya")
                || t.contains("remember me");
    }

    private String normalize(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9\\s]", "").replaceAll("\\s+", " ").trim();
    }

    private boolean isCreatorQuestion(String input) {
        String t = input.toLowerCase();
        return t.contains("who created you") || t.contains("who made you");
    }
}