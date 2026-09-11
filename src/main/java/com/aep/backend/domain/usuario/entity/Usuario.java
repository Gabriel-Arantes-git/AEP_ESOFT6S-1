package com.aep.backend.domain.usuario.entity;

import com.aep.backend.domain.abstraction.DefaultEntity;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collection;
import java.util.List;

@Document(collection = "usuario")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"authorities", "username", "enabled",
        "accountNonExpired", "accountNonLocked", "credentialsNonExpired"})
public class Usuario extends DefaultEntity implements UserDetails {

    private String nome;

    @Indexed(unique = true)
    private String email;

    @Indexed(unique = true)
    private String cpf;

    private String telefone;

    @JsonIgnore
    private String senhaHash;

    private PerfilUsuario perfil;

    private boolean ativo = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    @JsonIgnore
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isEnabled() {
        return ativo;
    }
}
