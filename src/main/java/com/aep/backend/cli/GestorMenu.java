package com.aep.backend.cli;

import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;

import java.util.Scanner;

public class GestorMenu {

    private final Scanner scanner;
    private final SolicitacaoService solicitacaoService;

    public GestorMenu(Scanner scanner, SolicitacaoService solicitacaoService) {
        this.scanner = scanner;
        this.solicitacaoService = solicitacaoService;
    }

    public void exibir(Usuario gestor) {
        while (true) {
            System.out.println("\n--- Gestor: " + gestor.getNome() + " ---");
            System.out.println("[1] Painel geral");
            System.out.println("[2] Ver fila por status");
            System.out.println("[3] Atualizar status");
            System.out.println("[4] Ver detalhes de solicitação");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> painelGeral();
                case "2" -> selecionarStatusEListar();
                case "3" -> atualizarStatus(gestor);
                case "4" -> verDetalhesSolicitacao();
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void painelGeral() {
        SolicitacaoPrinter.imprimirTabelaFila(solicitacaoService.listarTodas());
    }

    private void selecionarStatusEListar() {
        System.out.println("[1] ABERTO  [2] TRIAGEM  [3] EM_EXECUCAO  [4] RESOLVIDO  [5] ENCERRADO");
        StatusSolicitacao status = switch (scanner.nextLine().trim()) {
            case "1" -> StatusSolicitacao.ABERTO;
            case "2" -> StatusSolicitacao.TRIAGEM;
            case "3" -> StatusSolicitacao.EM_EXECUCAO;
            case "4" -> StatusSolicitacao.RESOLVIDO;
            case "5" -> StatusSolicitacao.ENCERRADO;
            default -> null;
        };
        if (status == null) {
            System.out.println("Status inválido.");
            return;
        }
        SolicitacaoPrinter.imprimirTabelaFila(solicitacaoService.listarPorStatus(status));
    }

    private void atualizarStatus(Usuario responsavel) {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            SolicitacaoPrinter.imprimirSolicitacao(s);

            System.out.println("Novo status:");
            System.out.println("[1] EM_EXECUCAO  [2] RESOLVIDO  [3] ENCERRADO");
            StatusSolicitacao novoStatus = switch (scanner.nextLine().trim()) {
                case "1" -> StatusSolicitacao.EM_EXECUCAO;
                case "2" -> StatusSolicitacao.RESOLVIDO;
                case "3" -> StatusSolicitacao.ENCERRADO;
                default -> throw new IllegalArgumentException("Status inválido.");
            };

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine().trim();
            if (comentario.isBlank()) {
                System.out.println("Comentário obrigatório.");
                return;
            }

            solicitacaoService.moverStatus(s.getId(), new MoverStatusRequest(novoStatus, comentario, null, null), responsavel);
            System.out.println("Status atualizado.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void verDetalhesSolicitacao() {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            SolicitacaoPrinter.imprimirSolicitacao(s);
            SolicitacaoPrinter.imprimirHistorico(solicitacaoService.buscarHistorico(s.getId()));

            System.out.print("\nVer logs de auditoria? [s/n]: ");
            if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                SolicitacaoPrinter.imprimirLogs(solicitacaoService.buscarLogs(s.getId()));
            }
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
