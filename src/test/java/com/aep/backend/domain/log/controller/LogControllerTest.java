package com.aep.backend.domain.log.controller;

import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.service.LogService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.infra.config.SecurityConfig;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LogController.class)
@Import(SecurityConfig.class)
class LogControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private LogService logService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean(name = "mongoMappingContext", enforceOverride = false)
    private MongoMappingContext mongoMappingContext;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
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
        when(logService.listarTodos()).thenReturn(List.of(log));

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
