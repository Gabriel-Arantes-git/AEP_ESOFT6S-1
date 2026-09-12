package com.aep.backend.domain.log.controller;

import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.service.LogService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class LogControllerTest {

    @InjectMocks
    private LogController logController;

    private MockMvc mockMvc;

    @Mock
    private LogService logService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(logController)
                        .addFilters(new com.aep.backend.TestSecurityFilter())
                        .build();
    }


    @Test
    @DisplayName("Deve retornar status 200 com a lista de logs quando o usuario tem perfil GESTOR")
    @WithMockUser(roles = "GESTOR")
    void deveRetornarStatus200ComListaDeLogsQuandoUsuarioTemPerfilGestor() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("Ana");
        LogAcao log = new LogAcao(usuario, "ABRIR_SOLICITACAO", "solicitacao", "sol-1", "DEN-2026-00001");
        log.setId("log-1");
        org.mockito.Mockito.lenient().when(logService.listarTodos()).thenReturn(List.of(log));

        mockMvc.perform(get("/logs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("log-1"))
                .andExpect(jsonPath("$[0].acao").value("ABRIR_SOLICITACAO"));
    }

    @Test
    @DisplayName("Deve retornar status 403 quando o usuario nao tem perfil GESTOR")
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarStatus403QuandoUsuarioNaoTemPerfilGestor() throws Exception {
        mockMvc.perform(get("/logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 403 quando a requisicao nao esta autenticada")
    void deveRetornarStatus403QuandoRequisicaoNaoEstaAutenticada() throws Exception {
        mockMvc.perform(get("/logs"))
                .andExpect(status().isForbidden());
    }
}
