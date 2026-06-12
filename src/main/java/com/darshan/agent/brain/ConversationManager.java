package com.darshan.agent.brain;

import com.darshan.agent.autonomy.GoalManager;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.context.ContextStore;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Component;

@Component
public class ConversationManager {

    private final ContextStore contextStore;
    private final SkillRouter router;
    private final GoalManager goalManager;

    public ConversationManager(
            ContextStore contextStore,
            SkillRouter router,
            GoalManager goalManager) {

        this.contextStore = contextStore;
        this.router = router;
        this.goalManager = goalManager;
    }

    /**
     * MAIN BRAIN ENTRYPOINT
     */
    public String process(String userMessage) throws Exception {

        ConversationContext context =
                contextStore.getContext();

        // 1️⃣ detect GOAL
        if (userMessage.startsWith("Goal:")) {

            String goal =
                    userMessage.replace("Goal:", "").trim();

            goalManager.createGoal(goal);

            return "🎯 Goal accepted. Shree is now working autonomously.";
        }

        // 2️⃣ choose intent
        String intent = detectIntent(userMessage);

        // 3️⃣ route skill
        Skill skill = router.route(intent);

        if (skill == null) {
            return "I don't know how to handle that yet.";
        }

        // 4️⃣ execute thinking
        return skill.execute(userMessage, context);
    }

    /**
     * Basic Intent Engine (Phase 1)
     */
    private String detectIntent(String message) {

        String lower = message.toLowerCase();

        if (lower.contains("study")
                || lower.contains("learn"))
            return "CHAT";

        if (lower.contains("hello")
                || lower.contains("hi"))
            return "GREETING";

        return "CHAT";
    }
}