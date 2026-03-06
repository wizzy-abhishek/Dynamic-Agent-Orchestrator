package com.dynamic_agent_orchestration.dao.controller;

import com.dynamic_agent_orchestration.dao.service.TaskAgentOrchestratorService;
import com.dynamic_agent_orchestration.dao.user_request_dto.TaskDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task/")
public class AgentBasedTaskController {

    private final TaskAgentOrchestratorService taskAgentOrchestratorService;

    public AgentBasedTaskController(TaskAgentOrchestratorService taskAgentOrchestratorService) {
        this.taskAgentOrchestratorService = taskAgentOrchestratorService;
    }

    @GetMapping("/agent-allocation")
    public ResponseEntity<String> taskAssignment(@RequestBody TaskDTO taskDTO){
        String response = taskAgentOrchestratorService.taskAllocator(taskDTO);
        return ResponseEntity.ok(response);
    }

}
