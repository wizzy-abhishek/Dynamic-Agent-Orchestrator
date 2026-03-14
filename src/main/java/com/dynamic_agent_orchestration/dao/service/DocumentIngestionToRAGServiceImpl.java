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

    private Document cleanDocumentText(Document doc) {
        String text = doc.getText();

        if (text != null) {
            text = text.replaceAll("[^\\x20-\\x7E\\t\\n\\r]", "");
            text = text.replaceAll(" {3,}", " ");
            text = text.replaceAll("\\n{3,}", "\n\n");
            text = text.replaceAll("\\n(?=[a-z])", " ");
        }

        return new Document(text, doc.getMetadata());
    }

    public List<Document> processAndSplitDocument(Resource resource) {
        try {
            log.info("Parsing and chunking document: {}", resource.getFilename());
            TikaDocumentReader documentReader = new TikaDocumentReader(resource);
            List<Document> rawDocuments = documentReader.get();

            List<Document> cleanedDocuments = rawDocuments.stream()
                    .map(this::cleanDocumentText)
                    .collect(Collectors.toList());

            List<Character> splitBoundaries = List.of('.', '!', '?', '\n');

            TokenTextSplitter tokenSplitter = new TokenTextSplitter(800,
                    300,
                    5,
                    10000,
                    true,
                    splitBoundaries);

            return tokenSplitter.apply(cleanedDocuments);
        } catch (Exception e) {
            log.error("Failed to process and split document: {}", resource.getFilename(), e);
            throw new RuntimeException("Failed to process document into chunks", e);
        }
    }
}
