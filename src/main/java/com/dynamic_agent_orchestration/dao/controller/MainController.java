package com.dynamic_agent_orchestration.dao.controller;

import com.dynamic_agent_orchestration.dao.service.AgentCreationService;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("create-agent/")
public class MainController {

    private final AgentCreationService agentCreationService;

    public MainController(AgentCreationService agentCreationService) {
        this.agentCreationService = agentCreationService;
    }

    @PostMapping()
    public String createAgent(@RequestBody UserRequestDTO prompt){
        return agentCreationService.assembleAgents(prompt);
    }
}
