package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.solicitacao.entity.Movimentacao;

import java.time.LocalDateTime;

public record MovimentacaoResponse(
        String id,
        String statusAnterior,
        String statusNovo,
        String comentario,
        String responsavelNome,
        LocalDateTime dataMovimentacao
) {
    public static MovimentacaoResponse from(Movimentacao m) {
        return new MovimentacaoResponse(
                m.getId(),
                m.getStatusAnterior() != null ? m.getStatusAnterior().name() : null,
                m.getStatusNovo().name(),
                m.getComentario(),
                m.getResponsavel() != null ? m.getResponsavel().nome() : null,
                m.getDataCadastro()
        );
    }
}
