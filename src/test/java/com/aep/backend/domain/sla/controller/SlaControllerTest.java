package com.aep.backend.domain.sla.controller;

import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.service.SlaService;
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
class SlaControllerTest {

    @InjectMocks
    private SlaController slaController;

    private MockMvc mockMvc;

    @Mock
    private SlaService slaService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(slaController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    @Test
    @DisplayName("Deve retornar status 200 com a lista de configuracoes de SLA")
    void deveRetornarStatus200ComListaDeConfiguracoesDeSla() throws Exception {
        SlaConfig sla = new SlaConfig(Prioridade.ALTA, 24, "Prazo para prioridade alta");
        sla.setId("sla-1");
        org.mockito.Mockito.lenient().when(slaService.listarTodos()).thenReturn(List.of(sla));

        mockMvc.perform(get("/sla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sla-1"))
                .andExpect(jsonPath("$[0].prioridade").value("ALTA"))
                .andExpect(jsonPath("$[0].prazoHoras").value(24));

        verify(slaService).listarTodos();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nao houver configuracoes de SLA")
    void deveRetornarListaVaziaQuandoNaoHouverConfiguracoesDeSla() throws Exception {
        org.mockito.Mockito.lenient().when(slaService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/sla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
