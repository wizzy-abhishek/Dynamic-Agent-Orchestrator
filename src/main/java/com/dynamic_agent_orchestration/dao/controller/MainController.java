package com.dynamic_agent_orchestration.dao.controller;

import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("create-agent/")
public class MainController {

    private final ChatClient client;

    public MainController(ChatClient client) {
        this.client = client;
    }

    @PostMapping()
    public String getAnswer(@RequestBody UserRequestDTO prompt){
        return "";
    }
}
