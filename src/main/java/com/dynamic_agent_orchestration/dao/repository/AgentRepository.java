package com.dynamic_agent_orchestration.dao.repository;

import com.dynamic_agent_orchestration.dao.entity.AgentStructureEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AgentRepository extends JpaRepository<AgentStructureEntity, Long> {

}
