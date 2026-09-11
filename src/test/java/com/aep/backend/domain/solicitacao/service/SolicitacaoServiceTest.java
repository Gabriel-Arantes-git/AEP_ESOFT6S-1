package com.aep.backend.domain.solicitacao.service;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.entity.DepartamentoResumo;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.repository.LogRepository;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.service.SlaService;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.repository.MovimentacaoRepository;
import com.aep.backend.domain.solicitacao.repository.SolicitacaoRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolicitacaoServiceTest {

    @Mock
    private SolicitacaoRepository solicitacaoRepository;

    @Mock
    private MovimentacaoRepository movimentacaoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private SlaService slaService;

    @Mock
    private LogRepository logRepository;

    @InjectMocks
    private SolicitacaoService solicitacaoService;

    @Test
    @DisplayName("Deve criar solicitação quando os dados forem válidos")
    void deveCriarSolicitacaoQuandoDadosForemValidos() {
        Categoria categoria = criarCategoria("cat-1");
        Usuario solicitante = criarUsuario("user-1", PerfilUsuario.CIDADAO);
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-1",
                "Descrição da solicitação com mais de cinquenta caracteres para análise válida.",
                "Centro",
                "Rua das Flores",
                "Próximo ao mercado",
                false,
                "Ana Souza",
                "ana@email.com",
                -23.56,
                -46.67,
                "01000-000"
        );
        when(categoriaRepository.findById("cat-1")).thenReturn(Optional.of(categoria));
        when(solicitacaoRepository.countByProtocoloStartingWith("DEN-" + Year.now().getValue() + "-")).thenReturn(0L);
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(logRepository.save(any(LogAcao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Solicitacao resultado = solicitacaoService.criar(request, solicitante);

        assertNotNull(resultado);
        assertEquals(StatusSolicitacao.ABERTO, resultado.getStatus());
        assertEquals(CategoriaResumo.from(categoria), resultado.getCategoria());
        assertEquals("user-1", resultado.getUsuarioId());
        assertTrue(resultado.getProtocolo().startsWith("DEN-" + Year.now().getValue() + "-"));
        verify(solicitacaoRepository).save(any(Solicitacao.class));
        verify(movimentacaoRepository).save(any(Movimentacao.class));
        verify(logRepository).save(any(LogAcao.class));
    }

    @Test
    @DisplayName("Não deve criar solicitação quando a descrição for nula")
    void naoDeveCriarSolicitacaoQuandoDescricaoForNula() {
        Usuario solicitante = criarUsuario("user-1", PerfilUsuario.CIDADAO);
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-1",
                null,
                "Centro",
                "Rua das Flores",
                "Próximo ao mercado",
                false,
                "Ana Souza",
                "ana@email.com",
                -23.56,
                -46.67,
                "01000-000"
        );

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> solicitacaoService.criar(request, solicitante)
        );

        assertEquals("Descrição obrigatória", excecao.getMessage());
    }

    @Test
    @DisplayName("Não deve criar solicitação quando a descrição anônima for curta")
    void naoDeveCriarSolicitacaoQuandoDescricaoAnonimaForCurta() {
        Usuario solicitante = criarUsuario("user-1", PerfilUsuario.CIDADAO);
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-1",
                "Descrição curta",
                "Centro",
                "Rua das Flores",
                "Próximo ao mercado",
                true,
                "Ana Souza",
                "ana@email.com",
                -23.56,
                -46.67,
                "01000-000"
        );

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> solicitacaoService.criar(request, solicitante)
        );

        assertEquals("Descrição deve ter no mínimo 50 caracteres para denúncia anônima", excecao.getMessage());
    }

    @Test
    @DisplayName("Não deve criar solicitação quando a categoria não existir")
    void naoDeveCriarSolicitacaoQuandoCategoriaNaoExistir() {
        Usuario solicitante = criarUsuario("user-1", PerfilUsuario.CIDADAO);
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-inexistente",
                "Descrição da solicitação com mais de cinquenta caracteres para análise válida.",
                "Centro",
                "Rua das Flores",
                "Próximo ao mercado",
                false,
                "Ana Souza",
                "ana@email.com",
                -23.56,
                -46.67,
                "01000-000"
        );
        when(categoriaRepository.findById("cat-inexistente")).thenReturn(Optional.empty());

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> solicitacaoService.criar(request, solicitante)
        );

        assertEquals("Categoria não encontrada: cat-inexistente", excecao.getMessage());
    }

    @Test
    @DisplayName("Deve mover status para triagem quando os dados forem válidos")
    void deveMoverStatusParaTriagemQuandoDadosForemValidos() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-1");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        Usuario responsavel = criarUsuario("user-2", PerfilUsuario.GESTOR);
        DepartamentoDestino departamento = new DepartamentoDestino("Obras", "Departamento de obras");
        departamento.setId("dep-1");
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.TRIAGEM,
                "Solicitação encaminhada para triagem",
                Prioridade.ALTA,
                "dep-1"
        );
        when(solicitacaoRepository.findById("sol-1")).thenReturn(Optional.of(solicitacao));
        when(departamentoRepository.findById("dep-1")).thenReturn(Optional.of(departamento));
        when(slaService.buscarPorPrioridade(Prioridade.ALTA)).thenReturn(new SlaConfig(Prioridade.ALTA, 12, "Prazo alto"));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(logRepository.save(any(LogAcao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        solicitacaoService.moverStatus("sol-1", request, responsavel);

        assertEquals(StatusSolicitacao.TRIAGEM, solicitacao.getStatus());
        assertEquals(Prioridade.ALTA, solicitacao.getPrioridade());
        assertEquals(DepartamentoResumo.from(departamento), solicitacao.getDepartamento());
        assertEquals(UsuarioResumo.from(responsavel), solicitacao.getAtendente());
        assertNotNull(solicitacao.getPrazoAlvo());
        verify(solicitacaoRepository).save(any(Solicitacao.class));
        verify(movimentacaoRepository).save(any(Movimentacao.class));
        verify(logRepository).save(any(LogAcao.class));
    }

    @Test
    @DisplayName("Deve encerrar solicitação quando o gestor confirmar a resolução")
    void deveEncerrarSolicitacaoQuandoGestorConfirmarResolucao() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-2");
        solicitacao.setStatus(StatusSolicitacao.RESOLVIDO);
        Usuario responsavel = criarUsuario("user-3", PerfilUsuario.GESTOR);
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.ENCERRADO,
                "Solicitação encerrada",
                null,
                null
        );
        when(solicitacaoRepository.findById("sol-2")).thenReturn(Optional.of(solicitacao));
        when(solicitacaoRepository.save(any(Solicitacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(movimentacaoRepository.save(any(Movimentacao.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(logRepository.save(any(LogAcao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        solicitacaoService.moverStatus("sol-2", request, responsavel);

        assertEquals(StatusSolicitacao.ENCERRADO, solicitacao.getStatus());
        assertNotNull(solicitacao.getDataEncerramento());
        assertTrue(solicitacao.getDataEncerramento().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Não deve mover status quando a transição for inválida")
    void naoDeveMoverStatusQuandoTransicaoForInvalida() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-3");
        solicitacao.setStatus(StatusSolicitacao.ENCERRADO);
        Usuario responsavel = criarUsuario("user-2", PerfilUsuario.GESTOR);
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.ABERTO,
                "Retorno inválido",
                null,
                null
        );
        when(solicitacaoRepository.findById("sol-3")).thenReturn(Optional.of(solicitacao));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> solicitacaoService.moverStatus("sol-3", request, responsavel)
        );

        assertEquals("Transição inválida: ENCERRADO → ABERTO", excecao.getMessage());
    }

    @Test
    @DisplayName("Não deve encerrar solicitação sem perfil de gestor")
    void naoDeveEncerrarSolicitacaoSemPerfilDeGestor() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-4");
        solicitacao.setStatus(StatusSolicitacao.RESOLVIDO);
        Usuario responsavel = criarUsuario("user-4", PerfilUsuario.ATENDENTE);
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.ENCERRADO,
                "Solicitação encerrada sem permissão",
                null,
                null
        );
        when(solicitacaoRepository.findById("sol-4")).thenReturn(Optional.of(solicitacao));

        IllegalStateException excecao = assertThrows(
                IllegalStateException.class,
                () -> solicitacaoService.moverStatus("sol-4", request, responsavel)
        );

        assertEquals("Apenas o gestor pode encerrar a solicitação", excecao.getMessage());
    }

    @Test
    @DisplayName("Não deve mover para triagem sem informar a prioridade")
    void naoDeveMoverParaTriagemSemInformarPrioridade() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-5");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        Usuario responsavel = criarUsuario("user-5", PerfilUsuario.GESTOR);
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.TRIAGEM,
                "Comentário",
                null,
                "dep-1"
        );
        when(solicitacaoRepository.findById("sol-5")).thenReturn(Optional.of(solicitacao));

        NullPointerException excecao = assertThrows(
                NullPointerException.class,
                () -> solicitacaoService.moverStatus("sol-5", request, responsavel)
        );

        assertEquals("Prioridade obrigatória na triagem", excecao.getMessage());
    }

    @Test
    @DisplayName("Não deve mover para triagem sem informar o departamento")
    void naoDeveMoverParaTriagemSemInformarDepartamento() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-6");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        Usuario responsavel = criarUsuario("user-6", PerfilUsuario.GESTOR);
        MoverStatusRequest request = new MoverStatusRequest(
                StatusSolicitacao.TRIAGEM,
                "Comentário",
                Prioridade.MEDIA,
                null
        );
        when(solicitacaoRepository.findById("sol-6")).thenReturn(Optional.of(solicitacao));

        NullPointerException excecao = assertThrows(
                NullPointerException.class,
                () -> solicitacaoService.moverStatus("sol-6", request, responsavel)
        );

        assertEquals("Departamento obrigatório na triagem", excecao.getMessage());
    }

    @Test
    @DisplayName("Deve buscar solicitação por protocolo quando ela existir")
    void deveBuscarSolicitacaoPorProtocoloQuandoExistir() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-7");
        solicitacao.setProtocolo("DEN-2026-00001");
        when(solicitacaoRepository.findByProtocolo("DEN-2026-00001")).thenReturn(Optional.of(solicitacao));

        Solicitacao resultado = solicitacaoService.buscarPorProtocolo("DEN-2026-00001");

        assertEquals("DEN-2026-00001", resultado.getProtocolo());
        assertEquals("sol-7", resultado.getId());
    }

    @Test
    @DisplayName("Não deve buscar solicitação por protocolo quando ela não existir")
    void naoDeveBuscarSolicitacaoPorProtocoloQuandoElaNaoExistir() {
        when(solicitacaoRepository.findByProtocolo("DEN-2026-99999")).thenReturn(Optional.empty());

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> solicitacaoService.buscarPorProtocolo("DEN-2026-99999")
        );

        assertEquals("Protocolo não encontrado: DEN-2026-99999", excecao.getMessage());
    }

    @Test
    @DisplayName("Deve listar solicitações por filtros disponíveis no serviço")
    void deveListarSolicitacoesPorFiltrosDisponiveisNoServico() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-8");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        solicitacao.setUsuarioId("user-1");
        when(solicitacaoRepository.findAllByOrderByDataCadastroDesc()).thenReturn(List.of(solicitacao));
        when(solicitacaoRepository.findAllByStatusOrderByDataCadastroAsc(StatusSolicitacao.ABERTO)).thenReturn(List.of(solicitacao));
        when(solicitacaoRepository.findAllByUsuarioIdOrderByDataCadastroDesc("user-1")).thenReturn(List.of(solicitacao));
        when(solicitacaoRepository.findAllByUsuarioIdIsNullOrderByDataCadastroDesc()).thenReturn(List.of());

        List<Solicitacao> todas = solicitacaoService.listarTodas();
        List<Solicitacao> porStatus = solicitacaoService.listarPorStatus(StatusSolicitacao.ABERTO);
        List<Solicitacao> minhas = solicitacaoService.listarMinhas("user-1");
        List<Solicitacao> anonimas = solicitacaoService.listarAnonimas();

        assertEquals(1, todas.size());
        assertEquals(1, porStatus.size());
        assertEquals(1, minhas.size());
        assertTrue(anonimas.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar histórico e logs da solicitação quando existirem registros")
    void deveBuscarHistoricoELogsDaSolicitacaoQuandoExistiremRegistros() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-9");
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setSolicitacaoId("sol-9");
        movimentacao.setStatusAnterior(StatusSolicitacao.ABERTO);
        movimentacao.setStatusNovo(StatusSolicitacao.TRIAGEM);
        LogAcao log = new LogAcao(criarUsuario("user-9", PerfilUsuario.GESTOR), "MOVER_STATUS", "solicitacao", "sol-9", "Abertura");
        when(movimentacaoRepository.findAllBySolicitacaoIdOrderByDataCadastroAsc("sol-9")).thenReturn(List.of(movimentacao));
        when(logRepository.findAllByEntidadeAndEntidadeIdOrderByDataCadastroAsc("solicitacao", "sol-9")).thenReturn(List.of(log));

        List<Movimentacao> historico = solicitacaoService.buscarHistorico("sol-9");
        List<LogAcao> logs = solicitacaoService.buscarLogs("sol-9");

        assertEquals(1, historico.size());
        assertEquals(StatusSolicitacao.TRIAGEM, historico.get(0).getStatusNovo());
        assertEquals(1, logs.size());
        assertEquals("MOVER_STATUS", logs.get(0).getAcao());
    }

    private Usuario criarUsuario(String id, PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNome("Usuario " + id);
        usuario.setPerfil(perfil);
        return usuario;
    }

    private Categoria criarCategoria(String id) {
        Categoria categoria = new Categoria("Infraestrutura", "Categoria de infraestrutura");
        categoria.setId(id);
        return categoria;
    }
}
