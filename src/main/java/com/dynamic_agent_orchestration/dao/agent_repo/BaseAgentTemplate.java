package com.dynamic_agent_orchestration.dao.agent_repo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

@Service
public class BaseAgentTemplate {

    public static ChatClient chatClientTemplate(ChatModel chatModel,
                                                String refinedPrompt,
                                                ChatOptions chatOptions){
        return ChatClient
                .builder(chatModel)
                .defaultOptions(chatOptions)
                .defaultSystem(refinedPrompt)
                .build();
    }
}
