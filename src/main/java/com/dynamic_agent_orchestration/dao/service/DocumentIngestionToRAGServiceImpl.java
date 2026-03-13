package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.service.interfaces.DocumentIngestionToRAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentIngestionToRAGServiceImpl implements DocumentIngestionToRAGService {

    private final Logger log = LoggerFactory.getLogger(DocumentIngestionToRAGServiceImpl.class);

    public String extractAbstract(Resource resource) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        String fullText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        return fullText.substring(0, Math.min(fullText.length(), 4000));
    }

    public List<Document> processAndSplitDocument(Resource resource) {
        try {
            log.info("Parsing and chunking document: {}", resource.getFilename());
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> documents = documentReader.get();

            TokenTextSplitter tokenSplitter = new TokenTextSplitter();
            return tokenSplitter.apply(documents);
        } catch (Exception e) {
            log.error("Failed to process and split document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to process document into chunks", e);
        }
    }
}
