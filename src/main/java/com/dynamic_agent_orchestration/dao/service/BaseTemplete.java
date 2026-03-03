package com.dynamic_agent_orchestration.dao.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class BaseTemplate {

    public static ChatClient ChatClientTemplate(){
        return ChatClient.builder().build();
    }
}
