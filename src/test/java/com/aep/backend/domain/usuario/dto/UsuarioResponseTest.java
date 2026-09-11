package com.aep.backend.domain.usuario.dto;

import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.usuario.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioResponseTest {

    @Test
    @DisplayName("Deve criar o usuario response a partir do usuario quando os dados forem validos")
    void deveCriarUsuarioResponseAPartirDoUsuarioQuandoDadosForemValidos() {
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("Ana");
        usuario.setEmail("ana@email.com");
        usuario.setCpf("12345678900");
        usuario.setTelefone("999999999");
        usuario.setPerfil(PerfilUsuario.GESTOR);
        usuario.setAtivo(true);

        UsuarioResponse response = UsuarioResponse.from(usuario);

        assertEquals("user-1", response.id());
        assertEquals("Ana", response.nome());
        assertEquals("ana@email.com", response.email());
        assertEquals("12345678900", response.cpf());
        assertEquals("999999999", response.telefone());
        assertEquals("GESTOR", response.perfil());
        assertTrue(response.ativo());
    }
}
