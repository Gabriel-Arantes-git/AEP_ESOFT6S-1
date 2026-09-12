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
import com.aep.backend.infra.exception.GlobalExceptionHandler;
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
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class SolicitacaoControllerTest {

    @InjectMocks
    private SolicitacaoController solicitacaoController;

    private MockMvc mockMvc;

    @Mock
    private SolicitacaoService solicitacaoService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    private Usuario cidadao;
    private Usuario atendente;
    private Usuario gestor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(solicitacaoController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new com.aep.backend.TestSecurityFilter())
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
    @DisplayName("Deve retornar status 403 ao tentar abrir uma denuncia identificada sem autenticacao")
    void deveRetornarStatus403AoTentarAbrirDenunciaIdentificadaSemAutenticacao() throws Exception {
        mockMvc.perform(post("/solicitacoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestValido()))
                .andExpect(status().isForbidden());

        verifyNoInteractions(solicitacaoService);
    }

    @Test
    @DisplayName("Deve retornar status 201 e forcar anonimato ao abrir uma denuncia anonima sem autenticacao")
    void deveRetornarStatus201EForcarAnonimatoAoAbrirDenunciaAnonimaSemAutenticacao() throws Exception {
        Solicitacao salva = criarSolicitacao();
        salva.setAnonimo(true);
        salva.setUsuarioId(null);
        org.mockito.Mockito.lenient().when(solicitacaoService.criar(any(), isNull())).thenReturn(salva);

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
    @WithMockUser(roles = "GESTOR")
    void deveListarTodasAsSolicitacoesQuandoPerfilEGestorENenhumStatusInformado() throws Exception {
        org.mockito.Mockito.lenient().when(solicitacaoService.listarTodas()).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarTodas();
        verify(solicitacaoService, never()).listarPorStatus(any());
    }

    @Test
    @DisplayName("Deve listar solicitacoes filtrando por status quando o perfil e ATENDENTE")
    @WithMockUser(roles = "ATENDENTE")
    void deveListarSolicitacoesFiltrandoPorStatusQuandoPerfilEAtendente() throws Exception {
        org.mockito.Mockito.lenient().when(solicitacaoService.listarPorStatus(StatusSolicitacao.TRIAGEM)).thenReturn(List.of(criarSolicitacao()));

        mockMvc.perform(get("/solicitacoes").param("status", "TRIAGEM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("sol-1"));

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.TRIAGEM);
    }

    @Test
    @DisplayName("Deve retornar status 403 ao listar solicitacoes quando o perfil e CIDADAO")
    void deveRetornarStatus403AoListarSolicitacoesQuandoPerfilECidadao() throws Exception {
        mockMvc.perform(get("/solicitacoes")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                cidadao, null, cidadao.getAuthorities()))))
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
    @DisplayName("Deve retornar status 403 ao listar minhas solicitacoes sem autenticacao")
    void deveRetornarStatus403AoListarMinhasSolicitacoesSemAutenticacao() throws Exception {
        mockMvc.perform(get("/solicitacoes/minhas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve listar as solicitacoes publicas sem autenticacao")
    void deveListarAsSolicitacoesPublicasSemAutenticacao() throws Exception {
        org.mockito.Mockito.lenient().when(solicitacaoService.listarTodas()).thenReturn(List.of(criarSolicitacao()));

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
        org.mockito.Mockito.lenient().when(solicitacaoService.listarAnonimas()).thenReturn(List.of(anonima));

        mockMvc.perform(get("/solicitacoes/anonimas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anonimo").value(true));

        verify(solicitacaoService).listarAnonimas();
    }

    @Test
    @DisplayName("Deve buscar uma solicitacao pelo protocolo quando o usuario esta autenticado")
    void deveBuscarUmaSolicitacaoPeloProtocoloQuandoUsuarioEstaAutenticado() throws Exception {
        org.mockito.Mockito.lenient().when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(criarSolicitacao());

        mockMvc.perform(get("/solicitacoes/protocolo/DEN-2026-00001")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                cidadao, null, cidadao.getAuthorities()))))
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
        org.mockito.Mockito.lenient().when(solicitacaoService.buscarPorProtocolo("DEN-2026-99999"))
                .thenThrow(new IllegalArgumentException("Protocolo não encontrado: DEN-2026-99999"));

        mockMvc.perform(get("/solicitacoes/protocolo/DEN-2026-99999")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                cidadao, null, cidadao.getAuthorities()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve retornar status 403 ao mover status quando o perfil e CIDADAO")
    void deveRetornarStatus403AoMoverStatusQuandoPerfilECidadao() throws Exception {
        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                cidadao, null, cidadao.getAuthorities())))
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
    @DisplayName("Deve retornar status 400 ao mover status com payload invalido")
    void deveRetornarStatus400AoMoverStatusComPayloadInvalido() throws Exception {
        mockMvc.perform(patch("/solicitacoes/sol-1/status")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                gestor, null, gestor.getAuthorities())))
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
        org.mockito.Mockito.lenient().when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of(m));

        mockMvc.perform(get("/solicitacoes/sol-1/movimentacoes")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                cidadao, null, cidadao.getAuthorities()))))
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
        org.mockito.Mockito.lenient().when(solicitacaoService.buscarLogs("sol-1")).thenReturn(List.of(log));

        mockMvc.perform(get("/solicitacoes/sol-1/logs")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                gestor, null, gestor.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("log-1"))
                .andExpect(jsonPath("$[0].acao").value("MOVER_STATUS"));

        verify(solicitacaoService).buscarLogs("sol-1");
    }

    @Test
    @DisplayName("Deve retornar status 403 ao buscar trilha de auditoria quando o perfil nao e GESTOR")
    void deveRetornarStatus403AoBuscarTrilhaDeAuditoriaQuandoPerfilNaoEGestor() throws Exception {
        mockMvc.perform(get("/solicitacoes/sol-1/logs")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                atendente, null, atendente.getAuthorities()))))
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