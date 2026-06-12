package com.darshan.agent.planner;

import java.util.ArrayList;
import java.util.List;

public class ExecutionPlan {

    private List<PlanStep> steps = new ArrayList<>();

    public void addStep(PlanStep step) {
        steps.add(step);
    }

    public List<PlanStep> getSteps() {
        return steps;
    }
}
