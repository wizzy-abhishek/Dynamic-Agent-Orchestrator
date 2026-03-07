package com.dynamic_agent_orchestration.dao.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatClientConfig {

    @Bean
    public ChatClient getChatClient(ChatClient.Builder cb){
        return cb.build();
    }

    @Bean
    @Primary
    public ChatModel baseChatModel(OpenAiApi openAiApi){
        return OpenAiChatModel.builder().openAiApi(openAiApi).build();
    }
}
