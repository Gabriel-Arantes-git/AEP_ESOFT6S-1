package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.solicitacao.entity.Localizacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRequest(
        @NotNull String categoriaId,
        @NotBlank String descricao,
        Localizacao localizacao,
        boolean anonimo,
        String nomeContato,
        String emailContato
) {}
