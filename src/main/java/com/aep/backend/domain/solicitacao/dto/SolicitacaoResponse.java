package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.solicitacao.entity.Localizacao;
import com.aep.backend.domain.solicitacao.entity.MovimentacaoResumo;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;

import java.time.LocalDateTime;
import java.util.List;

public record SolicitacaoResponse(
        String id,
        String protocolo,
        String status,
        String prioridade,
        LocalDateTime dataAbertura,
        LocalDateTime prazoAlvo,
        LocalDateTime dataEncerramento,
        String descricao,
        Localizacao localizacao,
        boolean anonimo,
        String nomeContato,
        String emailContato,
        CategoriaInfo categoria,
        DepartamentoInfo departamento,
        UsuarioInfo atendente,
        List<MovimentacaoResumo> historico
) {
    public record CategoriaInfo(String id, String nome) {}
    public record DepartamentoInfo(String id, String nome) {}
    public record UsuarioInfo(String id, String nome) {}

    public static SolicitacaoResponse from(Solicitacao s) {
        return new SolicitacaoResponse(
                s.getId(),
                s.getProtocolo(),
                s.getStatus().name(),
                s.getPrioridade() != null ? s.getPrioridade().name() : null,
                s.getDataCadastro(),
                s.getPrazoAlvo(),
                s.getDataEncerramento(),
                s.getDescricao(),
                s.getLocalizacao(),
                s.isAnonimo(),
                s.getNomeContato(),
                s.getEmailContato(),
                new CategoriaInfo(s.getCategoria().id(), s.getCategoria().nome()),
                s.getDepartamento() != null
                        ? new DepartamentoInfo(s.getDepartamento().id(), s.getDepartamento().nome())
                        : null,
                s.getAtendente() != null
                        ? new UsuarioInfo(s.getAtendente().id(), s.getAtendente().nome())
                        : null,
                s.getHistorico()
        );
    }
}
