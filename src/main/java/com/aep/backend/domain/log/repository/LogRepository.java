package com.aep.backend.domain.log.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.log.entity.LogAcao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogRepository extends DefaultCrudRepository<LogAcao> {
    List<LogAcao> findAllByEntidadeAndEntidadeIdOrderByDataCadastroAsc(String entidade, String entidadeId);
    List<LogAcao> findAllByOrderByDataCadastroDesc();
}
