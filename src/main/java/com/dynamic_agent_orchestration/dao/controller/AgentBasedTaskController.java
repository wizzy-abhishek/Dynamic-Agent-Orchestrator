package com.dynamic_agent_orchestration.dao.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task/")
public class AgentBasedTaskController {




    @GetMapping("/calculate")
    public ResponseEntity<String> calculate(){

        return ResponseEntity.ok("");
    }

}
