package com.aep.backend.domain.categoria.entity;

public record CategoriaResumo(String id, String nome) {

    public static CategoriaResumo from(Categoria categoria) {
        return new CategoriaResumo(categoria.getId(), categoria.getNome());
    }
}
