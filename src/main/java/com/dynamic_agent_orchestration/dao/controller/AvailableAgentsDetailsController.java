package com.dynamic_agent_orchestration.dao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("available-agents")
public class AvailableAgentsDetailsController {


    @GetMapping()
    public ResponseEntity<String> availableAgents(){

    }
}
