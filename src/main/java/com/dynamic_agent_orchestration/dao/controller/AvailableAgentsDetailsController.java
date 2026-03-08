package com.dynamic_agent_orchestration.dao.controller;

import com.dynamic_agent_orchestration.dao.responses.AgentDetails;
import com.dynamic_agent_orchestration.dao.service.AvailableAgentsDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/available-agents")
public class AvailableAgentsDetailsController {

    private final AvailableAgentsDetailsService agentsDetailsService;

    public AvailableAgentsDetailsController(AvailableAgentsDetailsService agentsDetailsService) {
        this.agentsDetailsService = agentsDetailsService;
    }

    @GetMapping()
    public ResponseEntity<List<AgentDetails>> availableAgents(){
        return ResponseEntity.ok(agentsDetailsService.getAllAgentsDetails());
    }
}
