package com.aep.backend.domain.categoria.service;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
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
class CategoriaServiceTest {

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private CategoriaService categoriaService;

    @Test
    @DisplayName("Deve listar categorias ativas quando existirem registros")
    void deveListarCategoriasAtivasQuandoExistiremRegistros() {
        Categoria categoria = new Categoria("Infraestrutura", "Categoria de infraestrutura");
        categoria.setAtivo(true);
        when(categoriaRepository.findAllByAtivoTrue()).thenReturn(List.of(categoria));

        List<Categoria> resultado = categoriaService.listarAtivas();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).isAtivo());
        verify(categoriaRepository).findAllByAtivoTrue();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver categorias ativas")
    void deveRetornarListaVaziaQuandoNaoHouverCategoriasAtivas() {
        when(categoriaRepository.findAllByAtivoTrue()).thenReturn(List.of());

        List<Categoria> resultado = categoriaService.listarAtivas();

        assertTrue(resultado.isEmpty());
        verify(categoriaRepository).findAllByAtivoTrue();
    }
}
