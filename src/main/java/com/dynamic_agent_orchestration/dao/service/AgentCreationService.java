package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AgentCreationService {

    private final ChatModel chatModel;

    public AgentCreationService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    public String assembleAgents(UserRequestDTO userRequestDTO){
       ChatClient baseClient = BaseTemplate.chatClientTemplate(chatModel, userRequestDTO.getAgentTask());
       return baseClient.prompt("Hello ".concat(userRequestDTO.getAgentName())).call().content();
    }


}
