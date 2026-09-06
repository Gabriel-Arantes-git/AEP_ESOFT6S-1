package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.log.entity.LogAcao;

import java.time.LocalDateTime;

public record LogAcaoResponse(
        String id,
        String acao,
        String entidade,
        String entidadeId,
        String detalhes,
        String usuarioNome,
        LocalDateTime dataAcao
) {
    public static LogAcaoResponse from(LogAcao l) {
        return new LogAcaoResponse(
                l.getId(),
                l.getAcao(),
                l.getEntidade(),
                l.getEntidadeId(),
                l.getDetalhes(),
                l.getUsuario() != null ? l.getUsuario().nome() : null,
                l.getDataCadastro()
        );
    }
}
