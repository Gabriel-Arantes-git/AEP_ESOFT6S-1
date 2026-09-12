package com.aep.backend.domain.departamento.controller;

import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.service.DepartamentoService;
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
class DepartamentoControllerTest {

    @InjectMocks
    private DepartamentoController departamentoController;

    private MockMvc mockMvc;

    @Mock
    private DepartamentoService departamentoService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(departamentoController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    @Test
    @DisplayName("Deve retornar status 200 com a lista de departamentos ativos")
    void deveRetornarStatus200ComListaDeDepartamentosAtivos() throws Exception {
        DepartamentoDestino departamento = new DepartamentoDestino("Obras", "Secretaria de obras");
        departamento.setId("dep-1");
        departamento.setAtivo(true);
        org.mockito.Mockito.lenient().when(departamentoService.listarAtivos()).thenReturn(List.of(departamento));

        mockMvc.perform(get("/departamentos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("dep-1"))
                .andExpect(jsonPath("$[0].ativo").value(true));

        verify(departamentoService).listarAtivos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver departamentos ativos")
    void deveRetornarListaVaziaQuandoNaoHouverDepartamentosAtivos() throws Exception {
        org.mockito.Mockito.lenient().when(departamentoService.listarAtivos()).thenReturn(List.of());

        mockMvc.perform(get("/departamentos/ativos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("Deve retornar status 200 com a lista ao listar todos os departamentos")
    void deveRetornarStatus200ComListaAoListarTodosOsDepartamentos() throws Exception {
        DepartamentoDestino departamento = new DepartamentoDestino("Obras", "Secretaria de obras");
        departamento.setId("dep-1");
        org.mockito.Mockito.lenient().when(departamentoService.listarTodos()).thenReturn(List.of(departamento));

        mockMvc.perform(get("/departamentos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("dep-1"));

        verify(departamentoService).listarTodos();
    }
}
