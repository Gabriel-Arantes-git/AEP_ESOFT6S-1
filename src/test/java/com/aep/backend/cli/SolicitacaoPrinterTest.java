package com.aep.backend.cli;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.departamento.entity.DepartamentoResumo;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoPrinterTest {

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void tearDown() {
        System.setOut(saidaOriginal);
    }

    @Test
    @DisplayName("Deve imprimir solicitacao com atendente e departamento quando preenchidos")
    void deveImprimirSolicitacaoComAtendenteEDepartamentoQuandoPreenchidos() {
        Solicitacao solicitacao = solicitacaoBase();
        solicitacao.setAtendente(new UsuarioResumo("user-1", "Ana"));
        solicitacao.setDepartamento(new DepartamentoResumo("dep-1", "Prefeitura"));
        solicitacao.setPrazoAlvo(LocalDateTime.now().plusDays(1));

        SolicitacaoPrinter.imprimirSolicitacao(solicitacao);

        String saidaTexto = saida.toString();
        assertTrue(saidaTexto.contains("DEN-2026-00001"));
        assertTrue(saidaTexto.contains("Atendente  : Ana"));
        assertTrue(saidaTexto.contains("Departamento alvo: Prefeitura"));
    }

    @Test
    @DisplayName("Deve imprimir departamento nao definido quando ausente")
    void deveImprimirDepartamentoNaoDefinidoQuandoAusente() {
        Solicitacao solicitacao = solicitacaoBase();

        SolicitacaoPrinter.imprimirSolicitacao(solicitacao);

        assertTrue(saida.toString().contains("Departamento alvo: Não definido"));
    }

    @Test
    @DisplayName("Deve imprimir anonimo quando a solicitacao for anonima")
    void deveImprimirAnonimoQuandoSolicitacaoForAnonima() {
        Solicitacao solicitacao = solicitacaoBase();
        solicitacao.setAnonimo(true);
        solicitacao.setUsuarioId(null);

        SolicitacaoPrinter.imprimirSolicitacao(solicitacao);

        assertTrue(saida.toString().contains("Aberto por: Anônimo"));
    }

    @Test
    @DisplayName("Nao deve imprimir nada ao imprimir historico vazio")
    void naoDeveImprimirNadaAoImprimirHistoricoVazio() {
        SolicitacaoPrinter.imprimirHistorico(List.of());

        assertEquals("", saida.toString());
    }

    @Test
    @DisplayName("Deve imprimir historico e justificativa de atraso quando presentes")
    void deveImprimirHistoricoEJustificativaDeAtrasoQuandoPresentes() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setStatusAnterior(StatusSolicitacao.TRIAGEM);
        movimentacao.setStatusNovo(StatusSolicitacao.EM_EXECUCAO);
        movimentacao.setComentario("Em execução");
        movimentacao.setJustificativaAtraso("Aguardando peça");
        movimentacao.setDataCadastro(LocalDateTime.now());

        SolicitacaoPrinter.imprimirHistorico(List.of(movimentacao));

        String saidaTexto = saida.toString();
        assertTrue(saidaTexto.contains("TRIAGEM → EM_EXECUCAO"));
        assertTrue(saidaTexto.contains("Justificativa de atraso: Aguardando peça"));
    }

    @Test
    @DisplayName("Deve imprimir mensagem de nenhum log quando lista de logs vazia")
    void deveImprimirMensagemDeNenhumLogQuandoListaDeLogsVazia() {
        SolicitacaoPrinter.imprimirLogs(List.of());

        assertTrue(saida.toString().contains("Nenhum log registrado."));
    }

    @Test
    @DisplayName("Deve imprimir sistema como autor quando o log nao tiver usuario")
    void deveImprimirSistemaComoAutorQuandoLogNaoTiverUsuario() {
        LogAcao log = new LogAcao(null, "ABRIR_SOLICITACAO", "solicitacao", "sol-1", "Solicitação criada");

        SolicitacaoPrinter.imprimirLogs(List.of(log));

        assertTrue(saida.toString().contains("por: sistema"));
    }

    @Test
    @DisplayName("Deve imprimir nome do usuario como autor quando o log tiver usuario")
    void deveImprimirNomeDoUsuarioComoAutorQuandoLogTiverUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        usuario.setPerfil(PerfilUsuario.GESTOR);
        LogAcao log = new LogAcao(usuario, "MOVER_STATUS", "solicitacao", "sol-1", "ABERTO -> TRIAGEM");

        SolicitacaoPrinter.imprimirLogs(List.of(log));

        assertTrue(saida.toString().contains("por: Ana"));
    }

    @Test
    @DisplayName("Deve imprimir mensagem de nenhuma solicitacao quando a fila estiver vazia")
    void deveImprimirMensagemDeNenhumaSolicitacaoQuandoFilaEstiverVazia() {
        SolicitacaoPrinter.imprimirTabelaFila(List.of());

        assertTrue(saida.toString().contains("Nenhuma solicitação encontrada."));
    }

    @Test
    @DisplayName("Deve imprimir cabecalho e linha quando a fila tiver itens")
    void deveImprimirCabecalhoELinhaQuandoFilaTiverItens() {
        Solicitacao solicitacao = solicitacaoBase();

        SolicitacaoPrinter.imprimirTabelaFila(List.of(solicitacao));

        String saidaTexto = saida.toString();
        assertTrue(saidaTexto.contains("Protocolo"));
        assertTrue(saidaTexto.contains("DEN-2026-00001"));
    }

    private Solicitacao solicitacaoBase() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-1");
        solicitacao.setProtocolo("DEN-2026-00001");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        solicitacao.setCategoria(new CategoriaResumo("cat-1", "Lixo Irregular"));
        solicitacao.setBairro("Centro");
        solicitacao.setDescricao("Descrição da denúncia");
        solicitacao.setDataCadastro(LocalDateTime.now());
        solicitacao.setUsuarioId("user-1");
        return solicitacao;
    }
}
