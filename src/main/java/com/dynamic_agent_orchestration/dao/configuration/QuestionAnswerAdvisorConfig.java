package com.dynamic_agent_orchestration.dao.configuration;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuestionAnswerAdvisorConfig {

    @Bean
    public QuestionAnswerAdvisor getRAG(VectorStore vectorStore){
        return QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(SearchRequest
                        .builder()
                        .similarityThreshold(0.9)
                        .build())
                .build();
    }

}
