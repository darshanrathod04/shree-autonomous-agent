package com.darshan.agent.autonomy;

import com.darshan.agent.brain.WorldModel;
import com.darshan.agent.cognition.*;
import com.darshan.agent.context.ConversationContext;
import com.darshan.agent.memory.ActivityFeed;
import com.darshan.agent.memory.EpisodicRecallEngine;
import com.darshan.agent.orchestrator.AgentOrchestrator;
import com.darshan.agent.router.SkillRouter;
import com.darshan.agent.skills.Skill;
import org.springframework.stereotype.Component;

@Component
public class AutonomousLoop {

    private final GoalManager goals;
    private final SkillRouter router;
    private final SubGoalPlanner planner;
    private final ReflectionEngine reflectionEngine;
    private final SelfGoalEngine selfGoalEngine;
    private final WorldModel worldModel;
    private final MotivationEngine motivationEngine;
    private final ActivityFeed activityFeed;
    private final MetaCognitionEngine metaCognition;
    private final EpisodicRecallEngine recall;

    public AutonomousLoop(
            GoalManager goals,
            SkillRouter router,
            SubGoalPlanner planner,
            ReflectionEngine reflectionEngine,
            SelfGoalEngine selfGoalEngine,
            WorldModel worldModel,
            MotivationEngine motivationEngine,
            ActivityFeed activityFeed,
            MetaCognitionEngine metaCognition,
            EpisodicRecallEngine recall
    ) {
        this.goals = goals;
        this.router = router;
        this.planner = planner;
        this.reflectionEngine = reflectionEngine;
        this.selfGoalEngine = selfGoalEngine;
        this.worldModel = worldModel;
        this.motivationEngine = motivationEngine;
        this.activityFeed = activityFeed;
        this.metaCognition = metaCognition;
        this.recall = recall;
    }
    // ===============================
// LOOP STABILITY GUARD
// ===============================
    private String lastThought = "";
    private int repeatedCount = 0;

    private void pauseAgent(String reason) {

        System.out.println("🛑 Agent paused: " + reason);

        try {
            Thread.sleep(5000); // cooldown
        } catch (InterruptedException ignored) {}
    }

    public String run(ConversationContext context) throws Exception {

        if (!goals.hasGoal()) {
            return null;
        }

        AgentGoal goal = goals.getGoal();

// ⭐ NEW — experience recall
        String memoryContext =
                recall.recallRelevant(goal.getDescription());

        worldModel.update("episodicContext", memoryContext);

        if (goal.getSubGoals().isEmpty()) {
            planner.generateSubGoals(goal, memoryContext);
            return "🧠 Plan created.";
        }

        SubGoal step = goal.nextPending();

        if (step == null) {
            goals.clearGoal();
            return "✅ Goal completed.";
        }

        activityFeed.add(
                "Completed step: " + step.getDescription()
        );

        worldModel.update(
                "lastCompletedStep",
                step.getDescription()
        );




        Skill skill = router.route("CHAT");

        if (skill == null) {
            return "⚠ No thinking skill.";
        }

        String result =
                skill.execute(step.getDescription(), context);

        Thought thought = new Thought(
                step.getDescription(),
                "Autonomous step execution",
                result,
                "AUTONOMOUS_LOOP"
        );

        // ===============================
// LOOP STABILITY CHECK
// ===============================
        // ===============================
// LOOP STABILITY CHECK
// ===============================
        String currentThought = result;

        if (currentThought.equals(lastThought)) {
            repeatedCount++;
        } else {
            repeatedCount = 0;
        }

        lastThought = currentThought;

        if (repeatedCount >= 3) {
            pauseAgent("Repeated autonomous thinking loop");
            return "⏸ Agent paused due to repetition.";
        }

        ReflectionResult reflection =
                reflectionEngine.reflect(
                        step.getDescription(),
                        result,
                        thought
                );

        MetaThought meta =
                metaCognition.observe(thought, reflection);

        motivationEngine.evaluate(result);

        selfGoalEngine.evaluateForGoal(meta);


        if (isCompleted(result)) {
            step.complete();
            return "✔ Step done: " + step.getDescription();
        }

        return "🧠 Progress: " + result;
    }

    private boolean isCompleted(String result) {
        if (result == null) return false;

        String lower = result.toLowerCase();

        return lower.contains("completed")
                || lower.contains("done")
                || lower.contains("finished")
                || lower.contains("success");
    }
}