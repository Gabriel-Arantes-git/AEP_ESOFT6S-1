package com.aep.backend.domain.sla.service;

import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SlaServiceTest {

    @Mock
    private SlaRepository slaRepository;

    @InjectMocks
    private SlaService slaService;

    @Test
    @DisplayName("Deve buscar SLA quando a prioridade existir")
    void deveBuscarSlaQuandoPrioridadeExistir() {
        SlaConfig sla = new SlaConfig(Prioridade.ALTA, 12, "Prazo para prioridade alta");
        when(slaRepository.findByPrioridade(Prioridade.ALTA)).thenReturn(Optional.of(sla));

        SlaConfig resultado = slaService.buscarPorPrioridade(Prioridade.ALTA);

        assertEquals(Prioridade.ALTA, resultado.getPrioridade());
        assertEquals(12, resultado.getPrazoHoras());
        verify(slaRepository).findByPrioridade(Prioridade.ALTA);
    }

    @Test
    @DisplayName("Não deve buscar SLA quando a prioridade não existir")
    void naoDeveBuscarSlaQuandoPrioridadeNaoExistir() {
        when(slaRepository.findByPrioridade(Prioridade.CRITICA)).thenReturn(Optional.empty());

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> slaService.buscarPorPrioridade(Prioridade.CRITICA)
        );

        assertEquals("SLA não configurado para: " + Prioridade.CRITICA, excecao.getMessage());
    }
}
