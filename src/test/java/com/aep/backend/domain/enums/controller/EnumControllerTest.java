package com.aep.backend.domain.enums.controller;

import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EnumController.class)
class EnumControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean(name = "mongoMappingContext", enforceOverride = false)
    private MongoMappingContext mongoMappingContext;

    @Test
    @DisplayName("Deve retornar todos os status de solicitacao na ordem do fluxo")
    void deveRetornarTodosOsStatusDeSolicitacaoNaOrdemDoFluxo() throws Exception {
        mockMvc.perform(get("/enums/status-solicitacao"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(5))
                .andExpect(jsonPath("$[0]").value("ABERTO"))
                .andExpect(jsonPath("$[1]").value("TRIAGEM"))
                .andExpect(jsonPath("$[2]").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$[3]").value("RESOLVIDO"))
                .andExpect(jsonPath("$[4]").value("ENCERRADO"));
    }

    @Test
    @DisplayName("Deve retornar todas as prioridades disponiveis")
    void deveRetornarTodasAsPrioridadesDisponiveis() throws Exception {
        mockMvc.perform(get("/enums/prioridades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("BAIXA"))
                .andExpect(jsonPath("$[1]").value("MEDIA"))
                .andExpect(jsonPath("$[2]").value("ALTA"))
                .andExpect(jsonPath("$[3]").value("CRITICA"));
    }
}
