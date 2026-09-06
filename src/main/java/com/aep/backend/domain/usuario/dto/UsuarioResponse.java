package com.aep.backend.domain.usuario.dto;

import com.aep.backend.domain.usuario.entity.Usuario;

public record UsuarioResponse(
        String id,
        String nome,
        String email,
        String cpf,
        String telefone,
        String perfil,
        boolean ativo
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getCpf(),
                u.getTelefone(),
                u.getPerfil().name(),
                u.isAtivo()
        );
    }
}
