package com.aep.backend.domain.solicitacao.entity;

public record Localizacao(
        String logradouro,
        String bairro,
        String cep,
        String referencia,
        Double latitude,
        Double longitude
) {}
