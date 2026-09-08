package com.aep.backend.domain.departamento.service;

import com.aep.backend.domain.abstraction.DefaultCrudService;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartamentoService extends DefaultCrudService<DepartamentoRepository, DepartamentoDestino> {

    private final DepartamentoRepository departamentoRepository;

    @Override
    protected DepartamentoRepository getRepository() {
        return departamentoRepository;
    }

    public List<DepartamentoDestino> listarAtivos() {
        return departamentoRepository.findAllByAtivoTrue();
    }


}
