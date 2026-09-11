package com.aep.backend.cli;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.service.DepartamentoService;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtendenteMenuTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    @Mock
    private DepartamentoService departamentoService;

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;
    private final Usuario atendente = criarAtendente();

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void tearDown() {
        System.setOut(saidaOriginal);
    }

    private AtendenteMenu criarMenu(String entrada) {
        return new AtendenteMenu(new Scanner(entrada), solicitacaoService, departamentoService);
    }

    private Solicitacao solicitacaoBase() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-1");
        solicitacao.setProtocolo("DEN-2026-00001");
        solicitacao.setCategoria(new CategoriaResumo("cat-1", "Lixo Irregular"));
        solicitacao.setBairro("Centro");
        solicitacao.setDescricao("Descricao");
        solicitacao.setDataCadastro(LocalDateTime.now());
        return solicitacao;
    }

    private static Usuario criarAtendente() {
        Usuario usuario = new Usuario();
        usuario.setId("user-2");
        usuario.setNome("Atendente Teste");
        usuario.setPerfil(PerfilUsuario.ATENDENTE);
        return usuario;
    }

    @Test
    @DisplayName("Deve listar fila de solicitacoes abertas quando opcao for um")
    void deveListarFilaDeSolicitacoesAbertasQuandoOpcaoForUm() {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.ABERTO)).thenReturn(List.of(solicitacaoBase()));

        criarMenu("1\n0\n").exibir(atendente);

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.ABERTO);
        assertTrue(saida.toString().contains("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Deve listar fila em triagem quando opcao for dois")
    void deveListarFilaEmTriagemQuandoOpcaoForDois() {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.TRIAGEM)).thenReturn(List.of());

        criarMenu("2\n0\n").exibir(atendente);

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.TRIAGEM);
    }

    @Test
    @DisplayName("Deve listar fila em execucao quando opcao for tres")
    void deveListarFilaEmExecucaoQuandoOpcaoForTres() {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.EM_EXECUCAO)).thenReturn(List.of());

        criarMenu("3\n0\n").exibir(atendente);

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.EM_EXECUCAO);
    }

    @Test
    @DisplayName("Deve assumir solicitacao quando dados forem validos")
    void deveAssumirSolicitacaoQuandoDadosForemValidos() {
        Solicitacao solicitacao = solicitacaoBase();
        DepartamentoDestino departamento = new DepartamentoDestino("Prefeitura", "Descricao");
        departamento.setId("dep-1");
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacao);
        when(departamentoService.listarAtivos()).thenReturn(List.of(departamento));

        criarMenu("4\nden-2026-00001\n3\n1\nEncaminhado para a prefeitura\n0\n").exibir(atendente);

        verify(solicitacaoService).moverStatus("sol-1",
                new MoverStatusRequest(StatusSolicitacao.TRIAGEM, "Encaminhado para a prefeitura", Prioridade.ALTA, "dep-1"),
                atendente);
        assertTrue(saida.toString().contains("Prefeitura"));
    }

    @Test
    @DisplayName("Nao deve assumir solicitacao quando a prioridade for invalida")
    void naoDeveAssumirSolicitacaoQuandoPrioridadeForInvalida() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("4\nden-2026-00001\n9\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Erro: Prioridade inválida."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Nao deve assumir solicitacao quando o departamento for invalido")
    void naoDeveAssumirSolicitacaoQuandoDepartamentoForInvalido() {
        DepartamentoDestino departamento = new DepartamentoDestino("Prefeitura", "Descricao");
        departamento.setId("dep-1");
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());
        when(departamentoService.listarAtivos()).thenReturn(List.of(departamento));

        criarMenu("4\nden-2026-00001\n1\n9\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Departamento inválido."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Nao deve assumir solicitacao quando o comentario estiver vazio")
    void naoDeveAssumirSolicitacaoQuandoComentarioEstiverVazio() {
        DepartamentoDestino departamento = new DepartamentoDestino("Prefeitura", "Descricao");
        departamento.setId("dep-1");
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());
        when(departamentoService.listarAtivos()).thenReturn(List.of(departamento));

        criarMenu("4\nden-2026-00001\n1\n1\n \n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Comentário obrigatório."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Deve atualizar status quando dados forem validos")
    void deveAtualizarStatusQuandoDadosForemValidos() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("5\nden-2026-00001\n1\nEm atendimento\n0\n").exibir(atendente);

        verify(solicitacaoService).moverStatus("sol-1",
                new MoverStatusRequest(StatusSolicitacao.EM_EXECUCAO, "Em atendimento", null, null),
                atendente);
        assertTrue(saida.toString().contains("Status atualizado."));
    }

    @Test
    @DisplayName("Nao deve atualizar status quando o status informado for invalido")
    void naoDeveAtualizarStatusQuandoStatusInformadoForInvalido() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("5\nden-2026-00001\n9\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Erro: Status inválido."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Nao deve atualizar status quando o comentario estiver vazio")
    void naoDeveAtualizarStatusQuandoComentarioEstiverVazio() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("5\nden-2026-00001\n1\n \n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Comentário obrigatório."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Deve ver detalhes da solicitacao quando o protocolo existir")
    void deveVerDetalhesDaSolicitacaoQuandoProtocoloExistir() {
        Solicitacao solicitacao = solicitacaoBase();
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacao);
        when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of());

        criarMenu("6\nden-2026-00001\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Nao deve ver detalhes quando o protocolo nao existir")
    void naoDeveVerDetalhesQuandoProtocoloNaoExistir() {
        when(solicitacaoService.buscarPorProtocolo(eq("DEN-2026-99999")))
                .thenThrow(new IllegalArgumentException("Protocolo não encontrado: DEN-2026-99999"));

        criarMenu("6\nden-2026-99999\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Erro: Protocolo não encontrado: DEN-2026-99999"));
    }

    @Test
    @DisplayName("Deve encerrar o menu quando a opcao for zero")
    void deveEncerrarMenuQuandoOpcaoForZero() {
        criarMenu("0\n").exibir(atendente);

        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Deve exibir mensagem de opcao invalida quando a opcao nao existir")
    void deveExibirMensagemDeOpcaoInvalidaQuandoOpcaoNaoExistir() {
        criarMenu("9\n0\n").exibir(atendente);

        assertTrue(saida.toString().contains("Opção inválida."));
    }
}
