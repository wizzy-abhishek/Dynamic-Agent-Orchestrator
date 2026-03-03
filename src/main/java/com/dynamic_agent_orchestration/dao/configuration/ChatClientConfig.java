package com.dynamic_agent_orchestration.dao.configuration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatClientConfig {
    @Bean
    public ChatClient getChatClient(ChatClient.Builder cb){
        return cb.build();
    }
}
