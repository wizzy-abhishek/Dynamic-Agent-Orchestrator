package com.dynamic_agent_orchestration.dao.service.interfaces;

import org.springframework.ai.chat.model.ChatModel;

public interface ProvideChatModelService {

    ChatModel resolveModel(String modelName);
}
