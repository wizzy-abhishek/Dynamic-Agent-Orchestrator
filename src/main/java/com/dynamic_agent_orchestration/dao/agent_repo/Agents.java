package com.dynamic_agent_orchestration.dao.agent_repo;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Agents {
    public final static Map<String, AgentInstance> agentCollection = new HashMap<>();
}
