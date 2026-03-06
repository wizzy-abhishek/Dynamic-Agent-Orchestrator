package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.user_request_dto.TaskDTO;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class TaskAgentOrchestratorService {


    private final ChatClient taskDistributorAgent;

    public TaskAgentOrchestratorService(ChatClient.Builder taskDistributorAgent) {

        var agents = Agents.agentCollection;

        StringBuilder agentAndDesc = new StringBuilder();

        for(AgentInstance ag: agents.values()){
            agentAndDesc.append(ag.agent()).append(" : ").append(ag.desc()).append("\n");
        }

        String agentPrompt = """
                 You are an agent that perform various tasks as per request.\s
                 You have various agents.
                """ +
                "You have " + agents.size() + " agents. " +
                "The agents are: " + agentAndDesc + " " +
                "If you don't have any agent that can perform that task. Refuse gently" ;

        this.taskDistributorAgent = taskDistributorAgent
                .defaultSystem(agentPrompt)
                .build();
    }

    public String taskAllocator(TaskDTO taskDTO){
        return taskDistributorAgent
                .prompt()
                .user(taskDTO.getUserPrompt())
                .call()
                .content();
    }

}
