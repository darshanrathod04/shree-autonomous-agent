package com.darshan.agent.autonomy;

import com.darshan.agent.cognition.MetaThought;
import org.springframework.stereotype.Component;

@Component
public class SelfGoalEngine {

    private final GoalManager goals;

    public SelfGoalEngine(GoalManager goals) {
        this.goals = goals;
    }

    public void evaluateForGoal(MetaThought meta) {

        if (meta == null) return;

        // already busy
        if (goals.hasGoal()) {
            return;
        }

        // create self-improvement goal only when failure detected
        if (!meta.isSuccessful()) {

            AgentGoal goal =
                    new AgentGoal(
                            "Improve response quality and reasoning"
                    );

            goals.setGoal(goal);

            System.out.println(
                    "🎯 Self Goal Created: " + goal.getDescription()
            );
        }
    }
}