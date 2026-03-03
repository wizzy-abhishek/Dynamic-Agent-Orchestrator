package com.dynamic_agent_orchestration.dao.agent_repo;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Agents {
    final static Map<String, ChatClient> agentCollection = new HashMap<>();
}
