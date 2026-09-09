package com.aep.backend.domain.sla.service;

import com.aep.backend.domain.abstraction.DefaultCrudService;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlaService extends DefaultCrudService<SlaRepository, SlaConfig> {

    private final SlaRepository slaRepository;

    @Override
    protected SlaRepository getRepository() {
        return slaRepository;
    }

    public SlaConfig buscarPorPrioridade(Prioridade prioridade) {
        return slaRepository.findByPrioridade(prioridade)
                .orElseThrow(() -> new IllegalArgumentException("SLA não configurado para: " + prioridade));
    }
}
