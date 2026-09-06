package com.aep.backend.domain.departamento.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepartamentoRepository extends DefaultCrudRepository<DepartamentoDestino> {
    List<DepartamentoDestino> findAllByAtivoTrue();
    boolean existsByNome(String nome);
}
