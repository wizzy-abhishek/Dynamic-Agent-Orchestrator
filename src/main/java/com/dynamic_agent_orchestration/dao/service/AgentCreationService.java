package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.agent_repo.BaseAgentTemplate;
import com.dynamic_agent_orchestration.dao.entity.AgentStructureEntity;
import com.dynamic_agent_orchestration.dao.repository.AgentRepository;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AgentCreationService {

    private final Map<String, ChatModel> modelRegister;
    private final ValidateAgentExistenceService validatorService;
    private final AgentRepository agentRepository;

    public AgentCreationService(Map<String, ChatModel> modelRegister,
                                ValidateAgentExistenceService validatorService,
                                AgentRepository agentRepository) {
        this.modelRegister = modelRegister;
        this.validatorService = validatorService;
        this.agentRepository = agentRepository;
    }

    public String assembleAgents(UserRequestDTO userRequestDTO) {

        String refinedPrompt = refineUserDescription(userRequestDTO.getAgentTask());

        for (AgentInstance existingAgent : Agents.agentCollection.values()) {
            boolean isDuplicate = validatorService
                    .validateAgentAsync(refinedPrompt, existingAgent.desc())
                    .join();

            if (isDuplicate) return "Agent Exists and ready to work";
        }

        ChatModel modelUsedInAgent = resolveModel(userRequestDTO.getModelName());
        var chatOption = generateChatOption(userRequestDTO.getTemperature());

        ChatClient baseClient = BaseAgentTemplate.chatClientTemplate(modelUsedInAgent,
                refinedPrompt, chatOption);

        String appropriateAgentName = getAppropriateAgentName(refinedPrompt);
        String llm = modelUsedInAgent.getClass().getSimpleName();
        AgentInstance agentInstance = new AgentInstance( appropriateAgentName,
                refinedPrompt, baseClient, llm);

        agentRepository.save(new AgentStructureEntity(appropriateAgentName, refinedPrompt,
                llm, chatOption.getTemperature()));

        Agents.agentCollection.put(agentInstance.name(), agentInstance);
        return baseClient.prompt("Hello ".concat(agentInstance.name())).call().content();
    }

    private String refineUserDescription(String prompt) {
        String defaultPrompt = """
                You are an agent that refines the user prompt to generate a good prompt that is good for an agent.
                You only task is to refine the prompt that can be use to create an agent.
                """;
        ChatClient client = ChatClient
                .builder(modelRegister.get("openai"))
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
                .builder(modelRegister.get("openai"))
                .defaultSystem(defaultPrompt)
                .build();

        return client
                .prompt(desc)
                .call()
                .content();
    }

    private ChatOptions generateChatOption (Double temperature){
        return ChatOptions
                .builder()
                .temperature(Optional
                        .ofNullable(temperature)
                        .orElse(0.3))
                .build();
    }

    private ChatModel resolveModel(String modelName) {
        return Optional.ofNullable(modelRegister.get(modelName))
                .orElse(modelRegister.get("openai"));
    }

}
