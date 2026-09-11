package com.aep.backend.domain.departamento.service;

import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
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
class DepartamentoServiceTest {

    @Mock
    private DepartamentoRepository departamentoRepository;

    @InjectMocks
    private DepartamentoService departamentoService;

    @Test
    @DisplayName("Deve listar departamentos ativos quando existirem registros")
    void deveListarDepartamentosAtivosQuandoExistiremRegistros() {
        DepartamentoDestino departamento = new DepartamentoDestino("Setor de Obras", "Departamento de obras");
        departamento.setAtivo(true);
        when(departamentoRepository.findAllByAtivoTrue()).thenReturn(List.of(departamento));

        List<DepartamentoDestino> resultado = departamentoService.listarAtivos();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isAtivo());
        verify(departamentoRepository).findAllByAtivoTrue();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver departamentos ativos")
    void deveRetornarListaVaziaQuandoNaoHouverDepartamentosAtivos() {
        when(departamentoRepository.findAllByAtivoTrue()).thenReturn(List.of());

        List<DepartamentoDestino> resultado = departamentoService.listarAtivos();

        assertTrue(resultado.isEmpty());
        verify(departamentoRepository).findAllByAtivoTrue();
    }
}
