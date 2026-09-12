package com.aep.backend.domain.enums.controller;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EnumControllerTest {

    @InjectMocks
    private EnumController enumController;

    private MockMvc mockMvc;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(enumController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

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
