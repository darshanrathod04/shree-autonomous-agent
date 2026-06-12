package com.darshan.agent.controller;

import com.darshan.agent.approval.ApprovalService;
import com.darshan.agent.dto.ApprovalRequest;
import com.darshan.agent.dto.ApprovalResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent")
public class AgentApprovalController {

    private final ApprovalService approvalService;



    public AgentApprovalController(ApprovalService approvalService) {
        this.approvalService = approvalService;
    }

    @PostMapping("/approve")
    public ApprovalResponse approveAction(@RequestBody ApprovalRequest request) {

        String result = approvalService.handleApproval(request.isApproved());

        return new ApprovalResponse(
                request.isApproved() ? "APPROVED" : "REJECTED",
                result
        );
    }



}
