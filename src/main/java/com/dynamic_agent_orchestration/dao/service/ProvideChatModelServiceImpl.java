package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.service.interfaces.ProvideChatModelService;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class ProvideChatModelServiceImpl implements ProvideChatModelService {

    private final Map<String, ChatModel> modelRegister;
    private static final String DEFAULT_MODEL = "openai";

    public ProvideChatModelServiceImpl(Map<String, ChatModel> modelRegister) {
        this.modelRegister = modelRegister;
    }

    public ChatModel resolveModel(String modelName) {
        return Optional.ofNullable(modelName)
                .map(modelRegister::get)
                .orElseGet(() -> modelRegister.get(DEFAULT_MODEL));
    }
}
