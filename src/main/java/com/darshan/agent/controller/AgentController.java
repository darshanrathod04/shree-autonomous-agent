package com.darshan.agent.controller;

import com.darshan.agent.brain.ConversationManager;
import com.darshan.agent.dto.AgentRequest;
import com.darshan.agent.dto.AgentResponse;
import com.darshan.agent.memory.ActivityFeed;
import com.darshan.agent.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService service;
    private final ActivityFeed activityFeed;
    private final ConversationManager manager;



    public AgentController(AgentService service , ConversationManager manager , ActivityFeed activityFeed) {
        this.service = service;
        this.activityFeed = activityFeed;
        this.manager = manager;
    }

    @PostMapping("/ask")
    public AgentResponse askAgent(@RequestBody AgentRequest request)
            throws Exception {

        String message = request.getMessage();

        if (message == null || message.isBlank()) {
            message = "hello";
        }

        return service.process(request.getMessage());
    }

    @GetMapping("/activity")
    public List<String> activity() {
        return activityFeed.getRecentActivity();
    }

}
