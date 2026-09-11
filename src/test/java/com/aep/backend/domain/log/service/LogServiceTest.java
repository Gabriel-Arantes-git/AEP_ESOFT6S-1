package com.aep.backend.domain.log.service;

import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.repository.LogRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private LogService logService;

    @Test
    @DisplayName("Deve listar logs em ordem decrescente quando existirem registros")
    void deveListarLogsEmOrdemDecrescenteQuandoExistiremRegistros() {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        LogAcao log = new LogAcao(usuario, "ABRIR_SOLICITACAO", "solicitacao", "abc123", "Solicitação criada");
        when(logRepository.findAllByOrderByDataCadastroDesc()).thenReturn(List.of(log));

        List<LogAcao> resultado = logService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("ABRIR_SOLICITACAO", resultado.get(0).getAcao());
        verify(logRepository).findAllByOrderByDataCadastroDesc();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver logs")
    void deveRetornarListaVaziaQuandoNaoHouverLogs() {
        when(logRepository.findAllByOrderByDataCadastroDesc()).thenReturn(List.of());

        List<LogAcao> resultado = logService.listarTodos();

        assertTrue(resultado.isEmpty());
        verify(logRepository).findAllByOrderByDataCadastroDesc();
    }
}
