package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.responses.AgentDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AvailableAgentsDetailsService {

    private final Map<String , AgentInstance> agents = Agents.agentCollection;

    public List<AgentDetails> getAllAgentsDetails() {
        return agents.values().stream()
                .map(this::getAgentDetail)
                .collect(Collectors.toList());
    }

    private AgentDetails getAgentDetail(AgentInstance agentInstance){
        return new AgentDetails(agentInstance.name(),
                agentInstance.desc(),
                agentInstance.llm());
    }

}
