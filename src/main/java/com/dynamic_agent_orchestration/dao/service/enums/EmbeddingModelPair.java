package com.dynamic_agent_orchestration.dao.service.enums;

import org.springframework.ai.chat.model.ChatModel;

public enum EmbeddingModelPair {

    ollamaEmbeddingModel("OllamaChatModel"),
    openAiEmbeddingModel("OpenAiChatModel");

    private final String chatModelName;

    EmbeddingModelPair(String chatModelName) {
        this.chatModelName = chatModelName;
    }

    public static String getEmbeddingName(String userInput) {
        for (EmbeddingModelPair pair : values()) {
            if (pair.chatModelName.equalsIgnoreCase(userInput)) {
                return pair.name();
            }
        }
        throw new IllegalArgumentException("No embedding mapping found for: " + userInput);
    }
}
