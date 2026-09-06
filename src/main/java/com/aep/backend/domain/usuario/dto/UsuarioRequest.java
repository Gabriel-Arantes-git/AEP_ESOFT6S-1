package com.aep.backend.domain.usuario.dto;

import com.aep.backend.domain.enums.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UsuarioRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @NotBlank String cpf,
        String telefone,
        @NotBlank String senha,
        @NotNull PerfilUsuario perfil
) {}
