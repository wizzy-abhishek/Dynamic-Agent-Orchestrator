package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.service.interfaces.PromptRefineryService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;


@Service
public class LlmPromptRefineService implements PromptRefineryService {

    public String refineUserDescription(String prompt) {
        String defaultPrompt = """
                You are an agent that refines the user prompt to generate a good prompt that is good for an agent.
                Your only task is to refine the prompt that can be used to create an agent.
                """;
        ChatClient client = ChatClient
                .builder(resolveModel(DEFAULT_MODEL))
                .defaultSystem(defaultPrompt)
                .build();

        return client.prompt(prompt).call().content();
    }

    public String generateAgentName(String desc) {
        String defaultPrompt = """
                You will be provided an agent description, you have to name it properly.
                Name it such that if in the future the description and the name of the agent are provided, you can differentiate if such an agent exists.
                The name should be readable, for example: agent_convert_currency, etc.
                """;
        ChatClient client = ChatClient
                .builder(resolveModel(DEFAULT_MODEL))
                .defaultSystem(defaultPrompt)
                .build();

        return client.prompt(desc).call().content();
    }
}
