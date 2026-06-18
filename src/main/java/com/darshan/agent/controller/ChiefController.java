package com.darshan.agent.controller;

import com.darshan.agent.chief.ChiefInsight;
import com.darshan.agent.chief.ChiefOfStaffEngine;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/agent/chief")
public class ChiefController {

    private final ChiefOfStaffEngine chiefEngine;

    public ChiefController(ChiefOfStaffEngine chiefEngine) {
        this.chiefEngine = chiefEngine;
    }

    @GetMapping("/insights")
    public Map<String, Object> getInsights() {
        Map<String, Object> result = new HashMap<>();
        List<ChiefInsight> all = chiefEngine.getAllInsights();
        result.put("insights", all);
        result.put("count", all.size());
        result.put("unresolved", chiefEngine.getUnresolvedCount());
        return result;
    }

    @GetMapping("/recommendation")
    public Map<String, Object> getRecommendation() {
        Map<String, Object> result = new HashMap<>();
        ChiefInsight rec = chiefEngine.getRecommendation();
        if (rec != null) {
            result.put("type", rec.getType().name());
            result.put("severity", rec.getSeverity().name());
            result.put("message", rec.getMessage());
            result.put("recommendation", rec.getRecommendation());
            result.put("priorityScore", rec.getPriorityScore());
        }
        return result;
    }

    @GetMapping("/risks")
    public Map<String, Object> getRisks() {
        Map<String, Object> result = new HashMap<>();
        List<ChiefInsight> risks = chiefEngine.getRisks();
        result.put("risks", risks);
        result.put("count", risks.size());
        return result;
    }

    @GetMapping("/summary")
    public Map<String, Object> getSummary() {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", chiefEngine.getSummary());
        result.put("insightCount", chiefEngine.getInsightCount());
        result.put("unresolvedCount", chiefEngine.getUnresolvedCount());
        return result;
    }

    @PostMapping("/resolve/{insightId}")
    public Map<String, Object> resolveInsight(@PathVariable String insightId) {
        Map<String, Object> result = new HashMap<>();
        boolean resolved = chiefEngine.resolveInsight(insightId);
        result.put("success", resolved);
        return result;
    }

    @PostMapping("/analyze")
    public Map<String, Object> triggerAnalysis() {
        chiefEngine.analyze();
        Map<String, Object> result = new HashMap<>();
        result.put("insights", chiefEngine.getInsightCount());
        result.put("unresolved", chiefEngine.getUnresolvedCount());
        return result;
    }
}