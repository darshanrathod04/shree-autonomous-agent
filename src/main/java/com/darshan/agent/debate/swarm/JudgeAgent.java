package com.darshan.agent.debate.swarm;

import java.util.Map;

public class JudgeAgent {
    public String selectBest(Map<String,String> outputs) {

        return outputs.entrySet()
                .stream()
                .max((a,b) ->
                        Integer.compare(
                                score(a.getValue()),
                                score(b.getValue())))
                .get()
                .getKey();
    }

    private int score(String text) {
        return text.length(); // simple heuristic
    }

}
