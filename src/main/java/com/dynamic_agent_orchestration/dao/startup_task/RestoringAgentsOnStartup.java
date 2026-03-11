package com.dynamic_agent_orchestration.dao.startup_task;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.repository.AgentRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RestoringAgentsOnStartup {

    private final AgentRepository agentRepository;
    private final Map<String, ChatModel> modelRegister;
    private final Map<String, AgentInstance> agents = Agents.agentCollection;

    public RestoringAgentsOnStartup(AgentRepository agentRepository,
                                    Map<String, ChatModel> modelRegister) {
        this.agentRepository = agentRepository;
        this.modelRegister = modelRegister;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void agentRestore() {

        agentRepository.findAll()
                .stream()
                .map(entity -> {
                    ChatClient chatClient = getChatClient(
                            entity.getLlm(),
                            entity.getTemperature(),
                            entity.getAgentDesc()
                    );
                    return getAgentInstance(
                            chatClient,
                            entity.getAgentName(),
                            entity.getAgentDesc(),
                            entity.getLlm()
                    );
                })
                .forEach(agent -> agents.put(agent.name(), agent));
    }

    private ChatClient getChatClient(String model, Double temp, String desc) {

        ChatModel chatModel = Optional
                .ofNullable(modelRegister.get(model))
                .orElse(modelRegister.get("openai"));

        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder()
                        .temperature(temp)
                        .build())
                .defaultSystem(desc)
                .build();
    }

    private AgentInstance getAgentInstance(ChatClient chatClient,
                                           String name,
                                           String desc,
                                           String llm) {
        return new AgentInstance(name, desc, chatClient, llm);
    }
}