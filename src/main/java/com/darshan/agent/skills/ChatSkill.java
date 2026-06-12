package com.darshan.agent.skills;

import com.darshan.agent.brain.BrainInterface;
import com.darshan.agent.brain.executive.ExecutiveDecision.Action;
import com.darshan.agent.brain.curiosity.CuriosityEngine;
import com.darshan.agent.brain.executive.ExecutiveControlEngine;
import com.darshan.agent.brain.executive.ExecutiveDecision;
import com.darshan.agent.cognition.ConceptExtractionEngine;
import com.darshan.agent.cognition.MetaCognitionEngine;
import com.darshan.agent.cognition.MetaThought;
import com.darshan.agent.cognition.MotivationEngine;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.llm.OllamaClient;
import com.darshan.agent.memory.EpisodicRecallEngine;
import com.darshan.agent.memory.UserProfile;
import com.darshan.agent.autonomy.SelfGoalEngine;
import com.darshan.agent.memory.EpisodicMemoryEngine;
import com.darshan.agent.memory.semantic.ConceptGraphEngine;
import com.darshan.agent.memory.semantic.SemanticMemoryEngine;
import com.darshan.agent.personality.PersonalityEngine;
import com.darshan.agent.self.SelfModelEngine;
import org.springframework.stereotype.Component;

@Component
public class ChatSkill implements Skill {

    private final BrainInterface brain;
    private final PersonalityEngine personalityEngine;
    private final UserProfile userProfile;
    private final MetaCognitionEngine metaCognition;
    private final MotivationEngine motivationEngine;
    private final ExecutiveControlEngine executive;
    private final SelfGoalEngine selfGoals;
    private final SelfModelEngine selfModel;
    private final EpisodicMemoryEngine episodicMemory;
    private final EpisodicRecallEngine recallEngine;
    private final SemanticMemoryEngine semantic;
    private final ConceptExtractionEngine conceptExtractor;
    private final ConceptGraphEngine conceptGraph;
    private final CuriosityEngine curiosity;


    public ChatSkill(
            BrainInterface brain,
            PersonalityEngine personalityEngine,
            UserProfile userProfile,
            MetaCognitionEngine metaCognition,
            MotivationEngine motivationEngine,
            ExecutiveControlEngine executive,
            SelfGoalEngine selfGoals,
            SelfModelEngine selfModel,
            EpisodicMemoryEngine episodicMemory,
            EpisodicRecallEngine recallEngine,
            SemanticMemoryEngine semantic,
            ConceptExtractionEngine conceptExtractor,
            ConceptGraphEngine conceptGraph,
            CuriosityEngine curiosity
    ) {
        this.brain = brain;
        this.personalityEngine = personalityEngine;
        this.userProfile = userProfile;
        this.metaCognition = metaCognition;
        this.motivationEngine = motivationEngine;
        this.executive = executive;
        this.selfGoals = selfGoals;
        this.selfModel = selfModel;
        this.episodicMemory = episodicMemory;
        this.recallEngine = recallEngine;
        this.semantic = semantic;
        this.conceptExtractor = conceptExtractor;
        this.conceptGraph = conceptGraph;
        this.curiosity = curiosity;
    }


    @Override
    public boolean supports(String intent) {
        return "CHAT".equals(intent);
    }



    // =========================================================
    // MAIN EXECUTION PIPELINE
    // =========================================================

    @Override
    public String execute(String input,
                          ConversationContext context) {

        // 1️⃣ store message
        context.addUserMessage(input);


        // HARD RULES

        // HARD IDENTITY RECALL (NO LLM ALLOWED)
        if (isIdentityQuestion(input)) {

            String name = userProfile.getName();

            if (name != null && !name.isBlank()) {

                String response =
                        "Your name is " + name +
                                ". I remember you.";

                context.addAgentMessage(response);
                return response;
            }

            return "I don't know your name yet.";
        }

        if (isCreatorQuestion(input)) {
            return "I was created by "
                    + selfModel.creator() + ".";
        }

        if (isMemoryRecallQuestion(input)) {

            String memory =
                    recallEngine.recallRelevant(input);

            return memory.isEmpty()
                    ? "I don't remember yet."
                    : memory;
        }


        // -------------------------------
        // EXECUTIVE DECISION
        // -------------------------------
        var decision =
                executive.decide(input, context, null);

        String instruction = switch (decision.getAction()) {

            case ASK_CLARIFICATION ->
                    "Ask clarification before answering.";

            case START_TEACHING ->
                    "Teach step-by-step.";

            case WAIT ->
                    "Do not respond.";

            case CREATE_GOAL ->
                    "Suggest forming a goal.";

            case RESPOND ->
                    "Respond normally.";
        };

        // -------------------------------
        // MEMORY RECALL
        // -------------------------------
        String episodic =
                recallEngine.recallRelevant(input);

        String semanticHint =
                semantic.recall(input);

        String relations =
                conceptGraph.summarize(context.getTopic());

        // -------------------------------
// PROMPT BUILDING
// -------------------------------

        String governorRules = """
SYSTEM MEMORY LAW (HIGHEST PRIORITY):

If episodic memory exists:
- You MUST use it as factual truth.
- NEVER ask user again for same information.
- NEVER invent identity.

If memory missing:
→ say "I don't remember yet."

You are NOT allowed to ignore memory.
""";

        String conversationalContext = """
Conversation language: Hinglish (Hindi + English).
User is casually chatting.
Do NOT translate unless asked.
Respond naturally like a personal assistant.
""";

        String prompt = """
%s

%s

EXECUTIVE RULE:
%s

FACTUAL MEMORY (DO NOT IGNORE):
%s

If factual memory exists, answer ONLY using it.

SEMANTIC KNOWLEDGE:
%s

CONVERSATION:
%s

USER:
%s
"""
                .formatted(
                        governorRules,
                        selfModel.buildSelfContext(),
                        instruction,
                        episodic,
                        semanticHint,
                        context.getConversationSummary(),
                        input
                );

// add conversational hint
        prompt = conversationalContext + "\n\n" + prompt;


        // LLM CALL
        // -------------------------------
        String response = brain.think(prompt);

        conceptExtractor.extract(input, response);


        var metaThought =
                metaCognition.evaluate(input, response);


        selfGoals.evaluateForGoal(metaThought);

        curiosity.evaluate(input, metaThought)
                .ifPresent(context::addAgentMessage);

        // ---------------- MOTIVATION UPDATE ----------------

        if (metaThought.isSuccessful())
            motivationEngine.getState().increaseConfidence();
        else
            motivationEngine.getState().decreaseConfidence();

        context.addAgentMessage(response);

        if (decision.getAction() == Action.RESPOND)
            return personalityEngine.applyPersonality(response);

        return response;
    }


    // =====================================================
    // HELPERS
    // =====================================================


    private boolean isMemoryRecallQuestion(String input) {
        String t = input.toLowerCase();

        return t.contains("kya bataya")
                || t.contains("what did i say")
                || t.contains("remember what");
    }

    private boolean isCompleted(String result) {

        if(result == null) return false;

        String r = result.toLowerCase();

        return r.contains("done")
                || r.contains("completed")
                || r.contains("finished")
                || r.contains("next step")
                || r.length() > 120; // long reasoning = progress
    }



    private boolean isIdentityQuestion(String input) {

        String t = normalize(input);

        return t.contains("who am i")
                || t.contains("what is my name")
                || t.contains("my name")
                || t.contains("mera naam")
                || t.contains("naam kya")
                || t.contains("remember me");
    }


    private String normalize(String input) {

        return input
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "") // remove punctuation
                .replaceAll("\\s+", " ")        // normalize spaces
                .trim();
    }


    private boolean isCreatorQuestion(String input) {
        String t = input.toLowerCase();
        return t.contains("who created you")
                || t.contains("who made you");
    }
}