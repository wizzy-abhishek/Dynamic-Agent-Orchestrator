package com.dynamic_agent_orchestration.dao.service.interfaces;

import org.springframework.ai.document.Document;
import org.springframework.core.io.Resource;

import java.util.List;

public interface DocumentIngestionToRAGService {

    String extractAbstract(Resource resource);
    List<Document> processAndSplitDocument(Resource resource);

}
