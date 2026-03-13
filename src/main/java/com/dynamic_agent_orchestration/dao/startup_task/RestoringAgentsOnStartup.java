package com.dynamic_agent_orchestration.dao.startup_task;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.repository.AgentRepository;
import com.dynamic_agent_orchestration.dao.service.enums.EmbeddingModelPair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class RestoringAgentsOnStartup {

    private static final Logger log = LoggerFactory.getLogger(RestoringAgentsOnStartup.class);

    private final JdbcTemplate jdbcTemplate;
    private final AgentRepository agentRepository;
    private final Map<String, ChatModel> modelRegister;
    private final Map<String, EmbeddingModel> embeddingModels;
    private final Map<String, AgentInstance> agents = Agents.agentCollection;

    public RestoringAgentsOnStartup(JdbcTemplate jdbcTemplate,
                                    AgentRepository agentRepository,
                                    Map<String, ChatModel> modelRegister,
                                    Map<String, EmbeddingModel> embeddingModels) {
        this.jdbcTemplate = jdbcTemplate;
        this.agentRepository = agentRepository;
        this.modelRegister = modelRegister;
        this.embeddingModels = embeddingModels;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void agentRestore() {
        agentRepository.findAll().forEach(entity -> {
            try {
                ChatClient chatClient = getChatClient(
                        entity.getLlm(),
                        entity.getTemperature(),
                        entity.getAgentDesc(),
                        entity.getAgentName(),
                        entity.hasVectorStore()
                );

                AgentInstance agent = getAgentInstance(
                        chatClient,
                        entity.getAgentName(),
                        entity.getAgentDesc(),
                        entity.getLlm()
                );

                agents.put(agent.name(), agent);
                log.info("Successfully restored agent: {}", agent.name());
            } catch (Exception e) {
                log.error("Failed to restore agent: {}", entity.getAgentName(), e);
            }
        });
    }

    private ChatClient getChatClient(String model, Double temp, String desc, String agentName, boolean hasVectorStore) {

        ChatModel chatModel = Optional
                .ofNullable(modelRegister.get(model))
                .orElse(modelRegister.get("openai"));

        ChatClient.Builder clientBuilder = ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder()
                        .temperature(Optional.ofNullable(temp).orElse(0.3))
                        .build())
                .defaultSystem(desc);

        String tableName = agentName.toLowerCase().replaceAll("\\s+", "_");

        if (hasVectorStore) {
            log.info("Found existing vector store for agent {}. Attaching advisor.", agentName);

            String embeddingModelName = EmbeddingModelPair.getEmbeddingName(model);
            EmbeddingModel embeddingModel = embeddingModels.get(embeddingModelName);

            if (embeddingModel != null) {
                int dimensions = embeddingModel.embed("test").length;

                PgVectorStore vectorStore = PgVectorStore.builder(jdbcTemplate, embeddingModel)
                        .vectorTableName(tableName)
                        .dimensions(dimensions)
                        .build();

                clientBuilder.defaultAdvisors(QuestionAnswerAdvisor.builder(vectorStore).build());
            } else {
                log.warn("Could not find embedding model '{}' for agent '{}'. Skipping advisor.", embeddingModelName, agentName);
            }
        } else {
            log.info("No vector store found for agent {}. Restoring without advisor.", agentName);
        }

        return clientBuilder.build();
    }

    private AgentInstance getAgentInstance(ChatClient chatClient,
                                           String name,
                                           String desc,
                                           String llm) {
        return new AgentInstance(name, desc, chatClient, llm);
    }
}