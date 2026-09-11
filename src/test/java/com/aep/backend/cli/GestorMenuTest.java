package com.aep.backend.cli;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.log.entity.LogAcao;
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
class GestorMenuTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;
    private final Usuario gestor = criarGestor();

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void tearDown() {
        System.setOut(saidaOriginal);
    }

    private GestorMenu criarMenu(String entrada) {
        return new GestorMenu(new Scanner(entrada), solicitacaoService);
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

    private static Usuario criarGestor() {
        Usuario usuario = new Usuario();
        usuario.setId("user-3");
        usuario.setNome("Gestor Teste");
        usuario.setPerfil(PerfilUsuario.GESTOR);
        return usuario;
    }

    @Test
    @DisplayName("Deve exibir painel geral quando opcao for um")
    void deveExibirPainelGeralQuandoOpcaoForUm() {
        when(solicitacaoService.listarTodas()).thenReturn(List.of(solicitacaoBase()));

        criarMenu("1\n0\n").exibir(gestor);

        verify(solicitacaoService).listarTodas();
        assertTrue(saida.toString().contains("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Deve listar por status quando o status informado for valido")
    void deveListarPorStatusQuandoStatusInformadoForValido() {
        when(solicitacaoService.listarPorStatus(StatusSolicitacao.RESOLVIDO)).thenReturn(List.of());

        criarMenu("2\n4\n0\n").exibir(gestor);

        verify(solicitacaoService).listarPorStatus(StatusSolicitacao.RESOLVIDO);
    }

    @Test
    @DisplayName("Nao deve listar quando o status informado for invalido")
    void naoDeveListarQuandoStatusInformadoForInvalido() {
        criarMenu("2\n9\n0\n").exibir(gestor);

        assertTrue(saida.toString().contains("Status inválido."));
        verify(solicitacaoService, never()).listarPorStatus(any());
    }

    @Test
    @DisplayName("Deve atualizar status quando dados forem validos, incluindo encerramento")
    void deveAtualizarStatusQuandoDadosForemValidosIncluindoEncerramento() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("3\nden-2026-00001\n3\nSolicitação resolvida e encerrada\n0\n").exibir(gestor);

        verify(solicitacaoService).moverStatus("sol-1",
                new MoverStatusRequest(StatusSolicitacao.ENCERRADO, "Solicitação resolvida e encerrada", null, null),
                gestor);
        assertTrue(saida.toString().contains("Status atualizado."));
    }

    @Test
    @DisplayName("Nao deve atualizar status quando o status informado for invalido")
    void naoDeveAtualizarStatusQuandoStatusInformadoForInvalido() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("3\nden-2026-00001\n9\n0\n").exibir(gestor);

        assertTrue(saida.toString().contains("Erro: Status inválido."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Nao deve atualizar status quando o comentario estiver vazio")
    void naoDeveAtualizarStatusQuandoComentarioEstiverVazio() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacaoBase());

        criarMenu("3\nden-2026-00001\n1\n \n0\n").exibir(gestor);

        assertTrue(saida.toString().contains("Comentário obrigatório."));
        verify(solicitacaoService, never()).moverStatus(any(), any(), any());
    }

    @Test
    @DisplayName("Deve exibir logs de auditoria quando confirmar a consulta")
    void deveExibirLogsDeAuditoriaQuandoConfirmarConsulta() {
        Solicitacao solicitacao = solicitacaoBase();
        LogAcao log = new LogAcao(gestor, "MOVER_STATUS", "solicitacao", "sol-1", "ABERTO -> TRIAGEM");
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacao);
        when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of());
        when(solicitacaoService.buscarLogs("sol-1")).thenReturn(List.of(log));

        criarMenu("4\nden-2026-00001\ns\n0\n").exibir(gestor);

        verify(solicitacaoService).buscarLogs("sol-1");
        assertTrue(saida.toString().contains("MOVER_STATUS"));
    }

    @Test
    @DisplayName("Nao deve exibir logs de auditoria quando nao confirmar a consulta")
    void naoDeveExibirLogsDeAuditoriaQuandoNaoConfirmarConsulta() {
        Solicitacao solicitacao = solicitacaoBase();
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacao);
        when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of());

        criarMenu("4\nden-2026-00001\nn\n0\n").exibir(gestor);

        verify(solicitacaoService, never()).buscarLogs(any());
    }

    @Test
    @DisplayName("Nao deve exibir detalhes quando o protocolo nao existir")
    void naoDeveExibirDetalhesQuandoProtocoloNaoExistir() {
        when(solicitacaoService.buscarPorProtocolo(eq("DEN-2026-99999")))
                .thenThrow(new IllegalArgumentException("Protocolo não encontrado: DEN-2026-99999"));

        criarMenu("4\nden-2026-99999\n0\n").exibir(gestor);

        assertTrue(saida.toString().contains("Erro: Protocolo não encontrado: DEN-2026-99999"));
    }

    @Test
    @DisplayName("Deve encerrar o menu quando a opcao for zero")
    void deveEncerrarMenuQuandoOpcaoForZero() {
        criarMenu("0\n").exibir(gestor);

        verify(solicitacaoService, never()).listarTodas();
    }

    @Test
    @DisplayName("Deve exibir mensagem de opcao invalida quando a opcao nao existir")
    void deveExibirMensagemDeOpcaoInvalidaQuandoOpcaoNaoExistir() {
        criarMenu("9\n0\n").exibir(gestor);

        assertTrue(saida.toString().contains("Opção inválida."));
    }
}
