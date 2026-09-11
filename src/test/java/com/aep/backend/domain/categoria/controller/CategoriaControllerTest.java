package com.aep.backend.domain.categoria.controller;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoriaController.class)
class CategoriaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoriaService categoriaService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean(name = "mongoMappingContext", enforceOverride = false)
    private MongoMappingContext mongoMappingContext;

    @Test
    @DisplayName("Deve retornar status 200 com a lista de categorias ativas")
    void deveRetornarStatus200ComListaDeCategoriasAtivas() throws Exception {
        Categoria categoria = new Categoria("Infraestrutura", "Categoria de infraestrutura");
        categoria.setId("cat-1");
        categoria.setAtivo(true);
        when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/categorias/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"))
                .andExpect(jsonPath("$[0].ativo").value(true));

        verify(categoriaService).listarAtivas();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver categorias ativas")
    void deveRetornarListaVaziaQuandoNaoHouverCategoriasAtivas() throws Exception {
        when(categoriaService.listarAtivas()).thenReturn(List.of());

        mockMvc.perform(get("/categorias/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
