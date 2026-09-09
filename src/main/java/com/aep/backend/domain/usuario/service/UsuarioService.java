package com.aep.backend.domain.usuario.service;

import com.aep.backend.domain.abstraction.DefaultCrudService;
import com.aep.backend.domain.usuario.dto.UsuarioRequest;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UsuarioService extends DefaultCrudService<UsuarioRepository, Usuario> {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    protected UsuarioRepository getRepository() {
        return usuarioRepository;
    }

    public Usuario cadastrar(UsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent())
            throw new IllegalArgumentException("E-mail já cadastrado.");
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setCpf(request.cpf());
        usuario.setTelefone(request.telefone());
        usuario.setSenhaHash(passwordEncoder.encode(request.senha()));
        usuario.setPerfil(request.perfil());
        return salvar(usuario);
    }
}
