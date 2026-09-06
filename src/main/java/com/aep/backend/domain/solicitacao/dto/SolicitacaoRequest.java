package com.aep.backend.domain.solicitacao.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SolicitacaoRequest(
        @NotNull String categoriaId,
        @NotBlank String descricao,
        String bairro,
        String logradouro,
        String referencia,
        boolean anonimo,
        String nomeContato,
        String emailContato,
        Double latitude,
        Double longitude,
        String cep
) {}
