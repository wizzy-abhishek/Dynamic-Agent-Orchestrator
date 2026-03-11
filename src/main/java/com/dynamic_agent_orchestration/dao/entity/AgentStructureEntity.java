package com.dynamic_agent_orchestration.dao.entity;

import jakarta.persistence.*;

@Entity
public class AgentStructureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String agentName;

    @Column(nullable = false,
            columnDefinition = "TEXT")
    private String agentDesc;

    @Column(nullable = false)
    private String llm;

    private Double temperature;

    public AgentStructureEntity() {
    }

    public AgentStructureEntity(String agentName, String agentDesc, String llm, Double temperature) {
        this.agentName = agentName;
        this.agentDesc = agentDesc;
        this.llm = llm;
        this.temperature = temperature;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentDesc() {
        return agentDesc;
    }

    public String getLlm() {
        return llm;
    }

    public Double getTemperature() {
        return temperature;
    }
}
