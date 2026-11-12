package com.aep.backend.domain.usuario.entity;

import com.aep.backend.domain.enums.PerfilUsuario;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioTest {

    private Usuario usuario(PerfilUsuario perfil) {
        Usuario u = new Usuario();
        u.setId("u-1");
        u.setNome("Maria");
        u.setEmail("maria@gov");
        u.setSenhaHash("hash");
        u.setPerfil(perfil);
        return u;
    }

    @Test
    void autoridadeDeveTerPrefixoRoleEPerfil() {
        Usuario u = usuario(PerfilUsuario.GESTOR);

        assertThat(u.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_GESTOR");
    }

    @Test
    void userDetailsDeveExporEmailComoUsernameESenhaHashComoPassword() {
        Usuario u = usuario(PerfilUsuario.CIDADAO);

        assertThat(u.getUsername()).isEqualTo("maria@gov");
        assertThat(u.getPassword()).isEqualTo("hash");
    }

    @Test
    void usuarioNovoDeveNascerAtivoEHabilitado() {
        Usuario u = usuario(PerfilUsuario.ATENDENTE);

        assertThat(u.isEnabled()).isTrue();
        assertThat(u.isAccountNonExpired()).isTrue();
        assertThat(u.isAccountNonLocked()).isTrue();
        assertThat(u.isCredentialsNonExpired()).isTrue();
    }

    @Test
    void usuarioInativoNaoDeveEstarHabilitado() {
        Usuario u = usuario(PerfilUsuario.CIDADAO);
        u.setAtivo(false);

        assertThat(u.isEnabled()).isFalse();
    }

    @Test
    void resumoDeveCopiarIdENome() {
        UsuarioResumo resumo = UsuarioResumo.from(usuario(PerfilUsuario.CIDADAO));

        assertThat(resumo.id()).isEqualTo("u-1");
        assertThat(resumo.nome()).isEqualTo("Maria");
    }

    @Test
    void resumoDeUsuarioNuloDeveSerNulo() {
        assertThat(UsuarioResumo.from(null)).isNull();
    }
}
