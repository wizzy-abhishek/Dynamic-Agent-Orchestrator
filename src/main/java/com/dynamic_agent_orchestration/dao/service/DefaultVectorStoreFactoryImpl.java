package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.service.interfaces.VectorStoreFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DefaultVectorStoreFactoryImpl implements VectorStoreFactory {

    private static final Logger log = LoggerFactory.getLogger(DefaultVectorStoreFactoryImpl.class);
    private final JdbcTemplate jdbcTemplate;

    public DefaultVectorStoreFactoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public VectorStore createTemporaryStore(EmbeddingModel embeddingModel) {
        log.info("Creating temporary SimpleVectorStore");
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Override
    public VectorStore createPersistentStore(EmbeddingModel embeddingModel, String agentName) {
        String tableName = agentName.toLowerCase().replaceAll("\\s+", "_");
        log.info("Creating persistent PgVectorStore for table: {}", tableName);

        int dimensions = embeddingModel.embed("test").length;

        PgVectorStore store = PgVectorStore
                .builder(jdbcTemplate, embeddingModel)
                .vectorTableName(tableName)
                .dimensions(dimensions)
                .initializeSchema(true)
                .build();

        try {
            store.afterPropertiesSet(); // instead of sql using this method abstraction
        } catch (Exception e) {
            log.error("Failed to initialize PgVector schema for table: {}", tableName, e);
            throw new RuntimeException("Database initialization failed for vector store", e);
        }

        return store;
    }
}
