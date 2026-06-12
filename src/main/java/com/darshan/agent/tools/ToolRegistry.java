package com.darshan.agent.tools;

import org.springframework.stereotype.Component;

@Component
public class ToolRegistry {

    public String suggestTask(String task) {
        return "Suggested Task: " + task;
    }

    public String createTodo(String todo) {
        return "TODO Created: " + todo;
    }
}
