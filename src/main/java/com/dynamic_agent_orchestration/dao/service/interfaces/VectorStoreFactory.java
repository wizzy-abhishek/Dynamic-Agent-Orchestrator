package com.dynamic_agent_orchestration.dao.service.interfaces;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;

public interface VectorStoreFactory {

    VectorStore createTemporaryStore(EmbeddingModel embeddingModel);
    VectorStore createPersistentStore(EmbeddingModel embeddingModel, String agentName);
}
