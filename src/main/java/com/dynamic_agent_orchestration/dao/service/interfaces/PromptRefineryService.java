package com.dynamic_agent_orchestration.dao.service.interfaces;

public interface PromptRefineryService {

    String refineUserDescription(String rawPrompt);
    String generateAgentName(String description);

}
