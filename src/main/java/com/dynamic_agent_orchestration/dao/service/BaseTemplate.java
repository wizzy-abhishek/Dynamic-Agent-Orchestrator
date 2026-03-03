package com.dynamic_agent_orchestration.dao.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class BaseTemplate {

    public static ChatClient chatClientTemplate(ChatModel chatModel, String refinedPrompt){
        return ChatClient.builder(chatModel).defaultSystem(refinedPrompt).build();
    }
}
