package com.darshan.agent.autonomy;

import org.springframework.stereotype.Component;

@Component
public class GoalManager {

    private AgentGoal currentGoal;

    public void setGoal(AgentGoal goal) {
        this.currentGoal = goal;
    }

    public boolean hasGoal() {
        return currentGoal != null && !currentGoal.isCompleted();
    }

    public AgentGoal getGoal() {
        return currentGoal;
    }

    public void clearGoal() {
        this.currentGoal = null;
    }
    public void createGoal(String description) {

        if (currentGoal != null) {
            return; // already working on something
        }

        currentGoal = new AgentGoal(description);

        System.out.println("🎯 New Goal Created: " + description);
    }
}
