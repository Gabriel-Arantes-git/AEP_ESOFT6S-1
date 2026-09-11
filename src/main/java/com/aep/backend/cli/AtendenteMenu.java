package com.aep.backend.cli;

import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.service.DepartamentoService;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;

import java.util.List;
import java.util.Scanner;

public class AtendenteMenu {

    private final Scanner scanner;
    private final SolicitacaoService solicitacaoService;
    private final DepartamentoService departamentoService;

    public AtendenteMenu(Scanner scanner, SolicitacaoService solicitacaoService, DepartamentoService departamentoService) {
        this.scanner = scanner;
        this.solicitacaoService = solicitacaoService;
        this.departamentoService = departamentoService;
    }

    public void exibir(Usuario atendente) {
        while (true) {
            System.out.println("\n--- Atendente: " + atendente.getNome() + " ---");
            System.out.println("[1] Ver fila (ABERTO)");
            System.out.println("[2] Ver fila (TRIAGEM)");
            System.out.println("[3] Ver fila (EM_EXECUCAO)");
            System.out.println("[4] Assumir solicitação (triagem)");
            System.out.println("[5] Atualizar status");
            System.out.println("[6] Ver detalhes de solicitação");
            System.out.println("[0] Sair");
            String opcao = scanner.nextLine().trim();

            switch (opcao) {
                case "1" -> listarPorStatus(StatusSolicitacao.ABERTO);
                case "2" -> listarPorStatus(StatusSolicitacao.TRIAGEM);
                case "3" -> listarPorStatus(StatusSolicitacao.EM_EXECUCAO);
                case "4" -> assumirSolicitacao(atendente);
                case "5" -> atualizarStatus(atendente);
                case "6" -> verDetalhesSolicitacao();
                case "0" -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void listarPorStatus(StatusSolicitacao status) {
        SolicitacaoPrinter.imprimirTabelaFila(solicitacaoService.listarPorStatus(status));
    }

    private void assumirSolicitacao(Usuario atendente) {
        System.out.print("Protocolo: ");
        String protocolo = scanner.nextLine().trim().toUpperCase();

        try {
            Solicitacao s = solicitacaoService.buscarPorProtocolo(protocolo);
            SolicitacaoPrinter.imprimirSolicitacao(s);

            System.out.println("\nPrioridade:");
            System.out.println("[1] BAIXA  [2] MEDIA  [3] ALTA  [4] CRITICA");
            Prioridade prioridade = switch (scanner.nextLine().trim()) {
                case "1" -> Prioridade.BAIXA;
                case "2" -> Prioridade.MEDIA;
                case "3" -> Prioridade.ALTA;
                case "4" -> Prioridade.CRITICA;
                default -> throw new IllegalArgumentException("Prioridade inválida.");
            };

            List<DepartamentoDestino> departamentos = departamentoService.listarAtivos();
            System.out.println("\nDepartamento destino:");
            for (int i = 0; i < departamentos.size(); i++) {
                System.out.printf("[%d] %s%n", i + 1, departamentos.get(i).getNome());
            }
            System.out.print("Departamento: ");
            int idx;
            try {
                idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
                if (idx < 0 || idx >= departamentos.size()) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                System.out.println("Departamento inválido.");
                return;
            }
            DepartamentoDestino departamento = departamentos.get(idx);

            System.out.print("Comentário: ");
            String comentario = scanner.nextLine().trim();
            if (comentario.isBlank()) {
                System.out.println("Comentário obrigatório.");
                return;
            }

            solicitacaoService.moverStatus(s.getId(),
                    new MoverStatusRequest(StatusSolicitacao.TRIAGEM, comentario, prioridade, departamento.getId()),
                    atendente);
            System.out.println("Solicitação encaminhada para " + departamento.getNome() + ". Prazo alvo definido.");
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
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
        } catch (Exception e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
