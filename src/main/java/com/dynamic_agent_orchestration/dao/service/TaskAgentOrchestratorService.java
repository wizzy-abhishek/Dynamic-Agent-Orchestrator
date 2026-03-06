package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.user_request_dto.TaskDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class TaskAgentOrchestratorService {

    private final Map<String, AgentInstance> agents = Agents.agentCollection;
    private final ChatClient.Builder chatClientBuilder;

    public TaskAgentOrchestratorService(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public String taskAllocator(TaskDTO taskDTO) {

        StringBuilder agentAndDesc = new StringBuilder();
        for(AgentInstance ag: agents.values()){
            agentAndDesc.append("- ").append(ag.name()).append(" : ").append(ag.desc()).append("\n");
        }

        String agentPrompt = """
            You are a strict internal routing system, NOT a conversational assistant.
            Your ONLY job is to analyze the user's task and select the name of the most appropriate agent.

            Available Agents:
            %s

            Rules:
            1. If an agent can handle the task, reply with EXACTLY and ONLY their name. No punctuation, no markdown, no pleasantries.
            2. If NO agent can handle the task, reply with EXACTLY "UNASSIGNED". Do not apologize. Do not attempt to answer the user's prompt yourself.
            """.formatted(agentAndDesc.toString());

        String routingDecision = chatClientBuilder
                .defaultSystem(agentPrompt)
                .build()
                .prompt()
                .user("Analyze this task and output the agent name: " + taskDTO.getUserPrompt())
                .call()
                .content();

        if (routingDecision == null || routingDecision.trim().equalsIgnoreCase("UNASSIGNED")) {
            return "You don't have any such agent to handle this task.";
        }

        var localAgent = agents.get(routingDecision.trim());

        if(localAgent == null) {
            return "Routing error: Router returned '" + routingDecision + "' which doesn't exist.";
        }

        return localAgent
                .agent()
                .prompt()
                .user(taskDTO.getUserPrompt())
                .call()
                .content();
    }
}