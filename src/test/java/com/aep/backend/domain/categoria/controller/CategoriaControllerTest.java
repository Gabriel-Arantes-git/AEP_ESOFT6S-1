package com.aep.backend.domain.categoria.controller;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    @InjectMocks
    private CategoriaController categoriaController;

    private MockMvc mockMvc;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(categoriaController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    @Test
    @DisplayName("Deve retornar status 200 com a lista de categorias ativas")
    void deveRetornarStatus200ComListaDeCategoriasAtivas() throws Exception {
        Categoria categoria = new Categoria("Infraestrutura", "Categoria de infraestrutura");
        categoria.setId("cat-1");
        categoria.setAtivo(true);
        org.mockito.Mockito.lenient().when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));

        mockMvc.perform(get("/categorias/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("cat-1"))
                .andExpect(jsonPath("$[0].ativo").value(true));

        verify(categoriaService).listarAtivas();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver categorias ativas")
    void deveRetornarListaVaziaQuandoNaoHouverCategoriasAtivas() throws Exception {
        org.mockito.Mockito.lenient().when(categoriaService.listarAtivas()).thenReturn(List.of());

        mockMvc.perform(get("/categorias/ativas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
