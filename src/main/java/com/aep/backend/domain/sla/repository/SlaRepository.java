package com.aep.backend.domain.sla.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SlaRepository extends DefaultCrudRepository<SlaConfig> {
    Optional<SlaConfig> findByPrioridade(Prioridade prioridade);
    boolean existsByPrioridade(Prioridade prioridade);
}
