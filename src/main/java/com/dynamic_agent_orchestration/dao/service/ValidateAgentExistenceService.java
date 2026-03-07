package com.dynamic_agent_orchestration.dao.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Service
public class ValidateAgentExistenceService {

    private final ChatModel chatModel;

    public ValidateAgentExistenceService(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @Async
    public CompletableFuture<Boolean> validateAgentAsync(String agentDesc1, String agentDesc2) {
        boolean exists = validate(agentDesc1, agentDesc2);
        return CompletableFuture.completedFuture(exists);
    }

    private boolean validate(String agent1, String agent2) {
        String defaultSys = """
                You are an agent that read description of two different agent and return only true or false.
                You validate if two agents are same or not, if they are same like they do same task, return true else false.
                By same task I mean if Agent A is a travel guide for India and Agent B guide for China return false.
                """;

        ChatClient agent = ChatClient
                .builder(chatModel)
                .defaultSystem(defaultSys)
                .build();

        String prompt = String.format("Agent 1: %s\nAgent 2: %s", agent1, agent2);

        String response = agent
                .prompt()
                .user(prompt)
                .call()
                .content();

        if (response == null) return false;

        return response
                .trim()
                .replace(".", "")
                .equalsIgnoreCase("true");
    }
}
