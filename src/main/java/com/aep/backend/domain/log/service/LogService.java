package com.aep.backend.domain.log.service;

import com.aep.backend.domain.abstraction.DefaultCrudService;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LogService extends DefaultCrudService<LogRepository, LogAcao> {

    private final LogRepository logRepository;

    @Override
    protected LogRepository getRepository() {
        return logRepository;
    }

    @Override
    public List<LogAcao> listarTodos() {
        return logRepository.findAllByOrderByDataCadastroDesc();
    }
}