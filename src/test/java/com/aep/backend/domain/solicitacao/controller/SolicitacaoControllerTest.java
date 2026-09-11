package com.aep.backend.domain.solicitacao.controller;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SolicitacaoController.class)
@Import(SecurityConfig.class)
class SolicitacaoControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @MockitoBean
    private SolicitacaoService solicitacaoService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @MockitoBean(name = "mongoMappingContext", enforceOverride = false)
    private MongoMappingContext mongoMappingContext;

    private Usuario cidadao;
    private Usuario atendente;
    private Usuario gestor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        cidadao = new Usuario();
        cidadao.setId("user-1");
        cidadao.setNome("Ana");
        cidadao.setEmail("ana@email.com");
        cidadao.setPerfil(PerfilUsuario.CIDADAO);

        atendente = new Usuario();
        atendente.setId("user-2");
        atendente.setNome("Bruno");
        atendente.setEmail("bruno@email.com");
        atendente.setPerfil(PerfilUsuario.ATENDENTE);

        gestor = new Usuario();
        gestor.setId("user-3");
        gestor.setNome("Carla");
        gestor.setEmail("carla@email.com");
        gestor.setPerfil(PerfilUsuario.GESTOR);
    }

    private Solicitacao criarSolicitacao() {
        Solicitacao s = new Solicitacao();
        s.setId("sol-1");
        s.setProtocolo("DEN-2026-00001");
        s.setCategoria(new CategoriaResumo("cat-1", "Infraestrutura"));
        s.setDescricao("Buraco na rua causando transtornos para os moradores da região");
        s.setStatus(StatusSolicitacao.ABERTO);
        s.setAnonimo(false);
        s.setUsuarioId("user-1");
        return s;
    }

    private String requestValido() {
        return """
                {"categoriaId":"cat-1","descricao":"Buraco na rua causando transtornos para os moradores da região",
                 "bairro":"Centro","logradouro":"Rua A","referencia":"Perto da praça","anonimo":false,
                 "nomeContato":"Ana","emailContato":"ana@email.com","latitude":-23.5,"longitude":-46.6,"cep":"01000-000"}
                """;
    }

    @Test
    @DisplayName("Deve retornar status 201 ao abrir uma denuncia identificada quando o usuario esta autenticado")
    void deveRetornarStatus201AoAbrirDenunciaIdentificadaQuandoUsuarioEstaAutenticado() throws Exception {
        Solicitacao salva = criarSolicitacao();
        when(solicitacaoService.criar(any(), eq(cidadao))).thenReturn(salva);

        mockMvc.perform(post("/solicitacoes")
                        .with(user(cidadao))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestValido()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("sol-1"))
                .andExpect(jsonPath("$.protocolo").value("DEN-2026-00001"))
                .andExpect(jsonPath("$.status").value("ABERTO"));

        verify(solicitacaoService).criar(any(), eq(cidadao));
    }

    @Test
    @DisplayName("Deve retornar status 403 ao tentar abrir uma denuncia identificada sem autenticacao")
    void deveRetornarStatus403AoTentarAbrirDenunciaIdentificadaSemAutenticacao() throws Exception {
        mockMvc.perform(post("/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestValido()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 400 ao abrir denuncia identificada com categoria inexistente")
    void deveRetornarStatus400AoAbrirDenunciaComCategoriaInexistente() throws Exception {
        when(solicitacaoService.criar(any(), eq(cidadao)))
                .thenThrow(new IllegalArgumentException("Categoria não encontrada: cat-1"));

        mockMvc.perform(post("/solicitacoes")
                        .with(user(cidadao))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestValido()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("Categoria não encontrada: cat-1"));
    }

    @Test
    @DisplayName("Deve retornar status 201 e forcar anonimato ao abrir uma denuncia anonima sem autenticacao")
    void deveRetornarStatus201EForcarAnonimatoAoAbrirDenunciaAnonimaSemAutenticacao() throws Exception {
        Solicitacao salva = criarSolicitacao();
        salva.setAnonimo(true);
        salva.setUsuarioId(null);
        when(solicitacaoService.criar(any(), isNull())).thenReturn(salva);

        String descricaoLonga = "Descrição bastante detalhada com mais de cinquenta caracteres para atender a validação.";
        mockMvc.perform(post("/solicitacoes/anonima")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"categoriaId":"cat-1","descricao":"%s","anonimo":false}
                                """.formatted(descricaoLonga)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anonimo").value(true));

        verify(solicitacaoService).criar(argThat(req -> req.anonimo()), isNull());
    }

    @Test
    @DisplayName("Deve listar todas as solicitacoes quando o perfil e GESTOR e nenhum status e informado")
    void deveListarTodasAsSolicitacoesQuandoPerfilEGestorENenhumStatusInformado() throws Exception {
        when(solicitacaoService.listarTodas()).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes").with(user(gestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarTodas();
        verify(solicitacaoService, never()).listarPorStatus(any());
    }

    @Test
    @DisplayName("Deve listar solicitacoes filtrando por status quando o perfil e ATENDENTE")
    void deveListarSolicitacoesFiltrandoPorStatusQuandoPerfilEAtendente() throws Exception {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.TRIAGEM)).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes").with(user(atendente)).param("status", "TRIAGEM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.TRIAGEM);
    }

    @Test
    @DisplayName("Deve retornar status 403 ao listar solicitacoes quando o perfil e CIDADAO")
    void deveRetornarStatus403AoListarSolicitacoesQuandoPerfilECidadao() throws Exception {
        mockMvc.perform(get("/solicitacoes").with(user(cidadao)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 403 ao listar solicitacoes sem autenticacao")
    void deveRetornarStatus403AoListarSolicitacoesSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve listar as solicitacoes do usuario autenticado")
    void deveListarAsSolicitacoesDoUsuarioAutenticado() throws Exception {
        when(solicitacaoService.listarMinhas("user-1")).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes/minhas").with(user(cidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarMinhas("user-1");
    }

    @Test
    @DisplayName("Deve retornar status 403 ao listar minhas solicitacoes sem autenticacao")
    void deveRetornarStatus403AoListarMinhasSolicitacoesSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes/minhas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve listar as solicitacoes publicas sem autenticacao")
    void deveListarAsSolicitacoesPublicasSemAutenticacao() throws Exception {
        when(solicitacaoService.listarTodas()).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes/publicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarTodas();
    }

    @Test
    @DisplayName("Deve listar apenas as solicitacoes anonimas sem autenticacao")
    void deveListarApenasAsSolicitacoesAnonimasSemAutenticacao() throws Exception {
        Solicitacao anonima = criarSolicitacao();
        anonima.setAnonimo(true);
        anonima.setUsuarioId(null);
        when(solicitacaoService.listarAnonimas()).thenReturn(List.of(anonima));

        mockMvc.perform(get("/solicitacoes/anonimas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anonimo").value(true));

        verify(solicitacaoService).listarAnonimas();
    }

    @Test
    @DisplayName("Deve buscar uma solicitacao pelo protocolo quando o usuario esta autenticado")
    void deveBuscarUmaSolicitacaoPeloProtocoloQuandoUsuarioEstaAutenticado() throws Exception {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(criarSolicitacao());

        mockMvc.perform(get("/solicitacoes/protocolo/DEN-2026-00001").with(user(cidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolo").value("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Deve retornar status 403 ao buscar por protocolo sem autenticacao")
    void deveRetornarStatus403AoBuscarPorProtocoloSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes/protocolo/DEN-2026-00001"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 400 quando o protocolo nao existir")
    void deveRetornarStatus400QuandoProtocoloNaoExistir() throws Exception {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-99999"))
                .thenThrow(new IllegalArgumentException("Protocolo não encontrado: DEN-2026-99999"));

        mockMvc.perform(get("/solicitacoes/protocolo/DEN-2026-99999").with(user(cidadao)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar status 204 ao mover o status de uma solicitacao quando o perfil e ATENDENTE")
    void deveRetornarStatus204AoMoverStatusQuandoPerfilEAtendente() throws Exception {
        doNothing().when(solicitacaoService).moverStatus(eq("sol-1"), any(), eq(atendente));

        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(user(atendente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"novoStatus":"TRIAGEM","comentario":"Iniciando triagem",
                                 "prioridade":"ALTA","departamentoId":"dep-1"}
                                """))
                .andExpect(status().isNoContent());

        verify(solicitacaoService).moverStatus(eq("sol-1"), argThat(req ->
                req.novoStatus() == StatusSolicitacao.TRIAGEM
                        && req.prioridade() == Prioridade.ALTA
                        && req.departamentoId().equals("dep-1")), eq(atendente));
    }

    @Test
    @DisplayName("Deve retornar status 403 ao mover status quando o perfil e CIDADAO")
    void deveRetornarStatus403AoMoverStatusQuandoPerfilECidadao() throws Exception {
        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(user(cidadao))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoStatus\":\"TRIAGEM\",\"comentario\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 403 ao mover status sem autenticacao")
    void deveRetornarStatus403AoMoverStatusSemAutenticacao() throws Exception {
        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoStatus\":\"TRIAGEM\",\"comentario\":\"Tentativa\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 409 quando a transicao de status for invalida")
    void deveRetornarStatus409QuandoTransicaoDeStatusForInvalida() throws Exception {
        doThrow(new IllegalStateException("Transição inválida: ABERTO → RESOLVIDO"))
                .when(solicitacaoService).moverStatus(eq("sol-1"), any(), eq(gestor));

        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(user(gestor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"novoStatus\":\"RESOLVIDO\",\"comentario\":\"Tentativa inválida\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Transição inválida: ABERTO → RESOLVIDO"));
    }

    @Test
    @DisplayName("Deve retornar status 400 ao mover status com payload invalido")
    void deveRetornarStatus400AoMoverStatusComPayloadInvalido() throws Exception {
        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(user(gestor))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"comentario\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar o historico de movimentacoes de uma solicitacao quando o usuario esta autenticado")
    void deveRetornarHistoricoDeMovimentacoesQuandoUsuarioEstaAutenticado() throws Exception {
        Movimentacao m = new Movimentacao();
        m.setId("mov-1");
        m.setSolicitacaoId("sol-1");
        m.setStatusAnterior(StatusSolicitacao.ABERTO);
        m.setStatusNovo(StatusSolicitacao.TRIAGEM);
        m.setComentario("Iniciando triagem");
        m.setResponsavel(UsuarioResumo.from(atendente));
        when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of(m));

        mockMvc.perform(get("/solicitacoes/sol-1/movimentacoes").with(user(cidadao)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("mov-1"))
                .andExpect(jsonPath("$[0].statusNovo").value("TRIAGEM"))
                .andExpect(jsonPath("$[0].responsavelNome").value("Bruno"));

        verify(solicitacaoService).buscarHistorico("sol-1");
    }

    @Test
    @DisplayName("Deve retornar status 403 ao buscar historico sem autenticacao")
    void deveRetornarStatus403AoBuscarHistoricoSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes/sol-1/movimentacoes"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar a trilha de auditoria de uma solicitacao quando o perfil e GESTOR")
    void deveRetornarTrilhaDeAuditoriaQuandoPerfilEGestor() throws Exception {
        LogAcao log = new LogAcao(gestor, "MOVER_STATUS", "solicitacao", "sol-1", "ABERTO → TRIAGEM");
        log.setId("log-1");
        when(solicitacaoService.buscarLogs("sol-1")).thenReturn(List.of(log));

        mockMvc.perform(get("/solicitacoes/sol-1/logs").with(user(gestor)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("log-1"))
                .andExpect(jsonPath("$[0].acao").value("MOVER_STATUS"));

        verify(solicitacaoService).buscarLogs("sol-1");
    }

    @Test
    @DisplayName("Deve retornar status 403 ao buscar trilha de auditoria quando o perfil nao e GESTOR")
    void deveRetornarStatus403AoBuscarTrilhaDeAuditoriaQuandoPerfilNaoEGestor() throws Exception {
        mockMvc.perform(get("/solicitacoes/sol-1/logs").with(user(atendente)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 403 ao buscar trilha de auditoria sem autenticacao")
    void deveRetornarStatus403AoBuscarTrilhaDeAuditoriaSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes/sol-1/logs"))
                .andExpect(status().isForbidden());
    }
}
