package com.dynamic_agent_orchestration.dao.configuration;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EmbeddingModelBean {

    @Bean
    @Primary
    public EmbeddingModel getEmbeddingModel(OpenAiApi openAiApi){
        return new OpenAiEmbeddingModel(openAiApi);
    }
}
