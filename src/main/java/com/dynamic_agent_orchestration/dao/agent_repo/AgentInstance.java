package com.dynamic_agent_orchestration.dao.agent_repo;

import org.springframework.ai.chat.client.ChatClient;

public record AgentInstance(String name,
                            String desc,
                            ChatClient agent,
                            String llm) {}
