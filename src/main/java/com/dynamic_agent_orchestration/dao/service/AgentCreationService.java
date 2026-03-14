package com.dynamic_agent_orchestration.dao.service;

import com.dynamic_agent_orchestration.dao.agent_repo.AgentInstance;
import com.dynamic_agent_orchestration.dao.agent_repo.Agents;
import com.dynamic_agent_orchestration.dao.agent_repo.BaseAgentTemplate;
import com.dynamic_agent_orchestration.dao.entity.AgentStructureEntity;
import com.dynamic_agent_orchestration.dao.repository.AgentRepository;
import com.dynamic_agent_orchestration.dao.service.enums.EmbeddingModelPair;
import com.dynamic_agent_orchestration.dao.service.interfaces.DocumentIngestionToRAGService;
import com.dynamic_agent_orchestration.dao.service.interfaces.PromptRefineryService;
import com.dynamic_agent_orchestration.dao.service.interfaces.ProvideChatModelService;
import com.dynamic_agent_orchestration.dao.service.interfaces.VectorStoreFactory;
import com.dynamic_agent_orchestration.dao.user_request_dto.UserRequestDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AgentCreationService {

    private static final Logger log = LoggerFactory.getLogger(AgentCreationService.class);

    private final AgentRepository agentRepository;
    private final VectorStoreFactory vectorStoreFactory;
    private final Map<String, EmbeddingModel> embeddingModels;
    private final PromptRefineryService promptRefineryService;
    private final ProvideChatModelService provideChatModelService;
    private final DocumentIngestionToRAGService documentIngestionToRAGService;

    public AgentCreationService(
            AgentRepository agentRepository,
            VectorStoreFactory vectorStoreFactory,
            Map<String, EmbeddingModel> embeddingModels,
            PromptRefineryService promptRefineryService,
            ProvideChatModelService provideChatModelService,
            DocumentIngestionToRAGService documentIngestionToRAGService) {

        this.agentRepository = agentRepository;
        this.vectorStoreFactory = vectorStoreFactory;
        this.embeddingModels = embeddingModels;
        this.promptRefineryService = promptRefineryService;
        this.documentIngestionToRAGService = documentIngestionToRAGService;
        this.provideChatModelService = provideChatModelService;
    }

    public String assembleAgents(UserRequestDTO userRequestDTO, MultipartFile file) {

        ChatModel modelUsedInAgent = provideChatModelService.resolveModel(userRequestDTO.getModelName());

        String refinedPrompt = promptRefineryService.refineUserDescription(userRequestDTO.getAgentTask());
        String appropriateAgentName = promptRefineryService.generateAgentName(refinedPrompt);
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
            agentRepository.save(new AgentStructureEntity(appropriateAgentName,
                    refinedPrompt,
                    llmName,
                    chatOption.getTemperature(),
                    !userRequestDTO.getTemporary()));
        }

        Agents.agentCollection.put(agentInstance.name(), agentInstance);

        return baseClient.prompt("Hello ".concat(agentInstance.name())).call().content();
    }

    private QuestionAnswerAdvisor extractFile(Resource file, String chatModelName, boolean temp, String agentName) {

        List<Document> chunks = documentIngestionToRAGService.processAndSplitDocument(file);

        String embeddingModelName = EmbeddingModelPair.getEmbeddingName(chatModelName);
        EmbeddingModel embeddingModel = embeddingModels.get(embeddingModelName);

        if (embeddingModel == null) {
            throw new IllegalArgumentException("No valid embedding model found for: " + chatModelName);
        }

        VectorStore vectorStore = temp ? vectorStoreFactory.createTemporaryStore(embeddingModel)
                : vectorStoreFactory.createPersistentStore(embeddingModel, agentName);

        vectorStore.add(chunks);
        return QuestionAnswerAdvisor.builder(vectorStore).build();
    }

    private ChatOptions generateChatOption(Double temperature) {
        return ChatOptions.builder()
                .temperature(Optional.ofNullable(temperature).orElse(0.3))
                .build();
    }
}