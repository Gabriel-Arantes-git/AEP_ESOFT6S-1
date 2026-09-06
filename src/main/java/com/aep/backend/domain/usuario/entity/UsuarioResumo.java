package com.aep.backend.domain.usuario.entity;

public record UsuarioResumo(String id, String nome) {

    public static UsuarioResumo from(Usuario usuario) {
        return usuario != null ? new UsuarioResumo(usuario.getId(), usuario.getNome()) : null;
    }
}
