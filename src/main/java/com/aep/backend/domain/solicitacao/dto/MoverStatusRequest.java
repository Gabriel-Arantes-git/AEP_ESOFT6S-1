package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MoverStatusRequest(
        @NotNull StatusSolicitacao novoStatus,
        @NotBlank String comentario,
        Prioridade prioridade,
        String departamentoId
) {}
