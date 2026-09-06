package com.aep.backend.domain.categoria.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.categoria.entity.Categoria;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends DefaultCrudRepository<Categoria> {
    List<Categoria> findAllByAtivoTrue();
    boolean existsByNome(String nome);
}
