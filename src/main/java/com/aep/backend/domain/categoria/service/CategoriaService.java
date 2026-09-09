package com.aep.backend.domain.categoria.service;

import com.aep.backend.domain.abstraction.DefaultCrudService;
import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService extends DefaultCrudService<CategoriaRepository, Categoria> {

    private final CategoriaRepository categoriaRepository;

    @Override
    protected CategoriaRepository getRepository() {
        return categoriaRepository;
    }

    public List<Categoria> listarAtivas() {
        return categoriaRepository.findAllByAtivoTrue();
    }


}
