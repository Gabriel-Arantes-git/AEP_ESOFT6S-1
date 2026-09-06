package com.aep.backend.domain.usuario.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends DefaultCrudRepository<Usuario> {
    Optional<Usuario> findByEmail(String email);
}
