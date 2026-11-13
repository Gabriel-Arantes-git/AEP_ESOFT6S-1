package com.aep.backend.cli;

import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.solicitacao.entity.Localizacao;
import com.aep.backend.domain.solicitacao.entity.MovimentacaoResumo;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;

import java.util.List;

public final class SolicitacaoPrinter {

    private SolicitacaoPrinter() {
    }

    public static void imprimirSolicitacao(Solicitacao s) {
        System.out.println("\nProtocolo : " + s.getProtocolo());
        System.out.println("Categoria : " + s.getCategoria().nome());
        System.out.println("Status    : " + s.getStatus());
        System.out.println("Prioridade: " + (s.getPrioridade() != null ? s.getPrioridade() : "-"));
        Localizacao local = s.getLocalizacao();
        System.out.println("Bairro    : " + (local != null ? local.bairro() : null));
        if (local != null && local.logradouro() != null) System.out.println("Logradouro: " + local.logradouro());
        System.out.println("Descrição : " + s.getDescricao());
        System.out.println("Aberta em : " + s.getDataCadastro().toLocalDate());
        String abertoPor = s.isAnonimo() ? "Anônimo" : (s.getUsuarioId() != null ? "Usuário " + s.getUsuarioId() : "Anônimo");
        System.out.println("Aberto por: " + abertoPor);
        if (s.getPrazoAlvo() != null) System.out.println("Prazo alvo: " + s.getPrazoAlvo().toLocalDate());
        if (s.getAtendente() != null) System.out.println("Atendente  : " + s.getAtendente().nome());
        if (s.getDepartamento() != null) System.out.println("Departamento alvo: " + s.getDepartamento().nome());
        else System.out.println("Departamento alvo: Não definido");
    }

    public static void imprimirHistorico(List<MovimentacaoResumo> historico) {
        if (historico.isEmpty()) return;
        System.out.println("\n--- Histórico ---");
        for (MovimentacaoResumo m : historico) {
            System.out.printf("[%s] %s → %s | %s%n",
                    m.dataMovimentacao().toLocalDate(),
                    m.statusAnterior() != null ? m.statusAnterior() : "-",
                    m.statusNovo(),
                    m.comentario());
            if (m.justificativaAtraso() != null) {
                System.out.println("  Justificativa de atraso: " + m.justificativaAtraso());
            }
        }
    }

    public static void imprimirLogs(List<LogAcao> logs) {
        if (logs.isEmpty()) {
            System.out.println("Nenhum log registrado.");
            return;
        }
        System.out.println("\n--- Logs de auditoria ---");
        for (LogAcao log : logs) {
            String autor = log.getUsuario() != null ? log.getUsuario().nome() : "sistema";
            System.out.printf("[%s] %s — %s | por: %s%n",
                    log.getDataCadastro() != null ? log.getDataCadastro().toLocalDate() : "-",
                    log.getAcao(),
                    log.getDetalhes() != null ? log.getDetalhes() : "-",
                    autor);
        }
    }

    public static void imprimirTabelaFila(List<Solicitacao> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma solicitação encontrada.");
            return;
        }
        System.out.printf("%n%-20s %-15s %-10s %-20s%n", "Protocolo", "Bairro", "Prioridade", "Categoria");
        System.out.println("-".repeat(70));
        for (Solicitacao s : lista) {
            System.out.printf("%-20s %-15s %-10s %-20s%n",
                    s.getProtocolo(),
                    s.getLocalizacao() != null ? s.getLocalizacao().bairro() : null,
                    s.getPrioridade() != null ? s.getPrioridade() : "-",
                    s.getCategoria().nome());
        }
    }
}
