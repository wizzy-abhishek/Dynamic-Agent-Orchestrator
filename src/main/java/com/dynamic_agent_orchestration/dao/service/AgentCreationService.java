package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.agent_repo.BaseAgentTemplate;
import com.dynamic_agent_orchestration.dao.entity.AgentStructureEntity;
import com.dynamic_agent_orchestration.dao.repository.AgentRepository;
import com.dynamic_agent_orchestration.dao.service.enums.EmbeddingModelPair;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentCreationService {

    private static final Logger log = LoggerFactory.getLogger(AgentCreationService.class);
    private static final String DEFAULT_MODEL = "openai";

    private final Map<String, ChatModel> modelRegister;
    private final AgentRepository agentRepository;
    private final Map<String, EmbeddingModel> embeddingModels;
    private final JdbcTemplate jdbcTemplate;

    public AgentCreationService(Map<String, ChatModel> modelRegister,
                                AgentRepository agentRepository,
                                Map<String, EmbeddingModel> embeddingModels,
                                JdbcTemplate jdbcTemplate) {

        this.modelRegister = modelRegister;
        this.agentRepository = agentRepository;
        this.embeddingModels = embeddingModels;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String assembleAgents(UserRequestDTO userRequestDTO, MultipartFile file) {

        ChatModel modelUsedInAgent = resolveModel(userRequestDTO.getModelName());
        String file_abstract = "";

        if (userRequestDTO.getAttachFile() && file != null && !file.isEmpty()) {
            try {
                file_abstract = "The file contains " + extractAbstract(file.getResource());
            } catch (Exception e) {
                log.error("Failed to extract file abstract", e);
                return "Error: Failed to read the attached file.";
            }
        }

        String refinedPrompt = refineUserDescription(userRequestDTO.getAgentTask() + file_abstract);
        String appropriateAgentName = getAppropriateAgentName(refinedPrompt);
        String llmName = modelUsedInAgent.getClass().getSimpleName();

        List<Advisor> advisors = new ArrayList<>();
        if (userRequestDTO.getAttachFile() && file != null && !file.isEmpty()) {
            try {
                advisors.add(extractFile(file.getResource(), llmName, userRequestDTO.getTemporary(), appropriateAgentName));
            } catch (Exception e) {
                log.error("Failed to extract file for agent context", e);
                return "Error: Failed to process the attached file into the vector store.";
            }
        }

        var chatOption = generateChatOption(userRequestDTO.getTemperature());
        ChatClient baseClient = BaseAgentTemplate.chatClientTemplate(modelUsedInAgent, refinedPrompt, chatOption, advisors);

        AgentInstance agentInstance = new AgentInstance(appropriateAgentName, refinedPrompt, baseClient, llmName);

        if(!userRequestDTO.getTemporary()){
            agentRepository.save(new AgentStructureEntity(appropriateAgentName, refinedPrompt, llmName, chatOption.getTemperature()));
        }

        Agents.agentCollection.put(agentInstance.name(), agentInstance);

        return baseClient.prompt("Hello ".concat(agentInstance.name())).call().content();
    }

    private String refineUserDescription(String prompt) {
        String defaultPrompt = """
                You are an agent that refines the user prompt to generate a good prompt that is good for an agent.
                Your only task is to refine the prompt that can be used to create an agent.
                """;
        ChatClient client = ChatClient
                .builder(resolveModel(DEFAULT_MODEL))
                .defaultSystem(defaultPrompt)
                .build();

        return client.prompt(prompt).call().content();
    }

    private String getAppropriateAgentName(String desc) {
        String defaultPrompt = """
                You will be provided an agent description, you have to name it properly.
                Name it such that if in the future the description and the name of the agent are provided, you can differentiate if such an agent exists.
                The name should be readable, for example: agent_convert_currency, etc.
                """;
        ChatClient client = ChatClient
                .builder(resolveModel(DEFAULT_MODEL))
                .defaultSystem(defaultPrompt)
                .build();

        return client.prompt(desc).call().content();
    }

    private QuestionAnswerAdvisor extractFile(Resource file, String chatModelName, boolean temp, String agentName) {
        DocumentReader documentReader = new TikaDocumentReader(file);
        List<Document> documents = documentReader.get();

        var tokenSplitter = new TokenTextSplitter();
        var chunks = tokenSplitter.apply(documents);

        String embeddingModelName = EmbeddingModelPair.getEmbeddingName(chatModelName);
        EmbeddingModel embeddingModel = embeddingModels.get(embeddingModelName);

        if (embeddingModel == null) {
            throw new IllegalArgumentException("No valid embedding model found for: " + chatModelName);
        }

        VectorStore vectorStore;

        if (temp) {
            log.info("Creating temporary SimpleVectorStore");
            vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        } else {
            log.info("Creating persistent PgVectorStore for table: {}", agentName);
            vectorStore = pgVectorStore(embeddingModel, agentName.toLowerCase().replaceAll("\\s+", "_"));
        }

        vectorStore.add(chunks);
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

    private ChatOptions generateChatOption(Double temperature) {
        return ChatOptions.builder()
                .temperature(Optional.ofNullable(temperature).orElse(0.3))
                .build();
    }

    private ChatModel resolveModel(String modelName) {
        return Optional.ofNullable(modelName)
                .map(modelRegister::get)
                .orElseGet(() -> modelRegister.get(DEFAULT_MODEL));
    }

    private String extractAbstract(Resource resource) {
        TikaDocumentReader reader = new TikaDocumentReader(resource);
        List<Document> documents = reader.get();

        String fullText = documents.stream()
                .map(Document::getText)
                .collect(Collectors.joining("\n"));

        return fullText.substring(0, Math.min(fullText.length(), 4000));
    }

    private PgVectorStore pgVectorStore(EmbeddingModel embeddingModel, String fileName){
        int dimensions = embeddingModel.embed("test").length;
        PgVectorStore store = PgVectorStore
                .builder(jdbcTemplate, embeddingModel)
                .vectorTableName(fileName)
                .dimensions(dimensions)
                .initializeSchema(true)
                .build();

        try {
            store.afterPropertiesSet(); // Instead of writing SQL to create, invoking this property to initialize
        } catch (Exception e) {
            log.error("Failed to initialize PgVector schema for table: {}", fileName, e);
            throw new RuntimeException("Database initialization failed", e);
        }

        return store;
    }
}