package com.dynamic_agent_orchestration.dao.user_request_dto;

public class UserRequestDTO {

    private String agentTask;
    private String llm;
    private String modelName;
    private Double temperature;

    public String getLlm() {
        return llm;
    }

    public String getModelName() {
        return modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getAgentTask() {
        return agentTask;
    }
}
