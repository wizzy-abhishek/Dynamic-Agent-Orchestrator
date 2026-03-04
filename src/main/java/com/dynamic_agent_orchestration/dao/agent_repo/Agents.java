package com.dynamic_agent_orchestration.dao.agent_repo;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Agents {
    public final static Map<String, AgentInstance> agentCollection = new ConcurrentHashMap<>();
}
