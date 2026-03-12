package com.dynamic_agent_orchestration.dao.controller;

import com.dynamic_agent_orchestration.dao.service.AgentCreationService;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("create-agent/")
public class MainController {

    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    private final AgentCreationService agentCreationService;

    public MainController(AgentCreationService agentCreationService) {
        this.agentCreationService = agentCreationService;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public String createAgent(@RequestPart("file") MultipartFile file, @RequestPart("dto") UserRequestDTO prompt){
        logger.info("Request to create an agent for: {}", prompt.getAgentTask());
        return agentCreationService.assembleAgents(prompt, file);
    }
}
