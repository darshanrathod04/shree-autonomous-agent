package com.darshan.agent.controller;

import com.darshan.agent.memory.MemoryStore;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentHistoryController {

    private final MemoryStore memoryStore;

    public AgentHistoryController(MemoryStore memoryStore) {
        this.memoryStore = memoryStore;
    }

    @GetMapping("/history")
    public Object history() {
        return memoryStore.load().getHistory();
    }
}
