package com.aep.backend.domain.departamento.entity;

public record DepartamentoResumo(String id, String nome) {

    public static DepartamentoResumo from(DepartamentoDestino departamento) {
        return new DepartamentoResumo(departamento.getId(), departamento.getNome());
    }
}
