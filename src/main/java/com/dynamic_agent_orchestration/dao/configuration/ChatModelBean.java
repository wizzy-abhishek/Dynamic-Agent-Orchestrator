package com.dynamic_agent_orchestration.dao.configuration;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatModelBean {

    @Bean (name = "openai")
    @Primary
    public ChatModel baseChatModel(OpenAiApi openAiApi){
        return OpenAiChatModel
                .builder()
                .openAiApi(openAiApi)
                .build();
    }

    @Bean (name = "ollama")
    public ChatModel ollamaModel(OllamaApi ollamaApi){
        return OllamaChatModel
                .builder()
                .ollamaApi(ollamaApi)
                .defaultOptions(OllamaChatOptions
                        .builder()
                        .model("llama3.1:8b")
                        .temperature(0.1)
                        .build())
                .build();
    }

}
