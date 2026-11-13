package com.aep.backend.domain.solicitacao.entity;

import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;

import java.time.LocalDateTime;

public record MovimentacaoResumo(
        String id,
        StatusSolicitacao statusAnterior,
        StatusSolicitacao statusNovo,
        String comentario,
        UsuarioResumo responsavel,
        String justificativaAtraso,
        LocalDateTime dataMovimentacao
) {

    public static MovimentacaoResumo from(Movimentacao m) {
        return new MovimentacaoResumo(
                m.getId(),
                m.getStatusAnterior(),
                m.getStatusNovo(),
                m.getComentario(),
                m.getResponsavel(),
                m.getJustificativaAtraso(),
                m.getDataCadastro()
        );
    }
}
