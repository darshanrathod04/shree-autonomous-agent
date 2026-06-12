package com.darshan.agent.controller;

import com.darshan.agent.orchestrator.AgentOrchestrator;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent/multi")
public class MultiAgentController {

    private final AgentOrchestrator orchestrator;

    public MultiAgentController(AgentOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/run")
    public String runMultiAgent(@RequestBody String task) throws Exception {
        return orchestrator.runTask(task);
    }
}
