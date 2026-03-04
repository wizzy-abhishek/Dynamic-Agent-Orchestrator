package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

@Service
public class AgentCreationService {

    private final ChatModel chatModel;
    private final ValidateAgentExistenceService validatorService;

    public AgentCreationService(ChatModel chatModel, ValidateAgentExistenceService validatorService) {
        this.chatModel = chatModel;
        this.validatorService = validatorService;
    }

    public String assembleAgents(UserRequestDTO userRequestDTO) {
        String refinedPrompt = refineUserDescription(userRequestDTO.getAgentTask());
        for (AgentInstance existingAgent : Agents.agentCollection.values()) {

            boolean isDuplicate = validatorService
                    .validateAgentAsync(refinedPrompt, existingAgent.desc())
                    .join();

            if (isDuplicate) {
                System.out.println("Found duplicate agent! Reusing: " + existingAgent.name());
                return existingAgent.agent().prompt("Hello " + existingAgent.name()).call().content();
            }
        }
        ChatClient baseClient = BaseTemplate.chatClientTemplate(chatModel, refinedPrompt);
        String appropriateAgentName = getAppropriateAgentName(refinedPrompt);
        AgentInstance agentInstance = new AgentInstance("", appropriateAgentName, refinedPrompt, baseClient);
        Agents.agentCollection.put(agentInstance.name(), agentInstance);
        return baseClient.prompt("Hello ".concat(agentInstance.name())).call().content();
    }

    private String refineUserDescription(String prompt) {
        String defaultPrompt = """
                You are an agent that refines the user prompt to generate a good prompt that is good for an agent.
                You only task is to refine the prompt that can be use to create an agent.
                """;
        ChatClient client = ChatClient
                .builder(chatModel)
                .defaultSystem(defaultPrompt)
                .build();

        return client
                .prompt(prompt)
                .call()
                .content();
    }

    private String getAppropriateAgentName(String desc) {
        String defaultPrompt = """
                You will be provided an agent description, you have to name it properly.
                Name as such, if in future that description and the name of the agent is provided you can differentiate if such agent exist.
                Name should be readable for example: agent_convert_currency, etc.
                \s""";
        ChatClient client = ChatClient
                .builder(chatModel)
                .defaultSystem(defaultPrompt)
                .build();

        return client
                .prompt(desc)
                .call()
                .content();
    }

}
