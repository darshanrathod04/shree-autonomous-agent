package com.darshan.agent.debate;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DebateMemory {

    private final List<DebateTurn> turns = new ArrayList<>();

    public void add(String agent, String msg) {
        turns.add(new DebateTurn(agent, msg));
    }

    public String transcript() {
        StringBuilder sb = new StringBuilder();

        for (DebateTurn t : turns) {
            sb.append(t.getAgent())
                    .append(": ")
                    .append(t.getMessage())
                    .append("\n");
        }

        return sb.toString();
    }

    public void clear() {
        turns.clear();
    }
}
