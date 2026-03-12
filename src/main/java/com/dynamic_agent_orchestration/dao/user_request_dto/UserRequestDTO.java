package com.dynamic_agent_orchestration.dao.user_request_dto;

public class UserRequestDTO {

    private String agentTask;
    private String modelName;
    private Double temperature;
    private Boolean attachFile;
    private Boolean temporary;

    public String getModelName() {
        return modelName;
    }

    public Double getTemperature() {
        return temperature;
    }

    public String getAgentTask() {
        return agentTask;
    }

    public Boolean getAttachFile() {
        if(attachFile ==  null) return false;

        return attachFile;
    }

    public Boolean getTemporary() {
        if(temporary == null) return true;
        return temporary;
    }
}
