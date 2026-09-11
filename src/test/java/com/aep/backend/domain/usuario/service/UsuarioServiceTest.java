package com.aep.backend.domain.usuario.service;

import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.usuario.dto.UsuarioRequest;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve cadastrar usuário quando o email não estiver em uso")
    void deveCadastrarUsuarioQuandoEmailNaoEstiverEmUso() {
        UsuarioRequest request = new UsuarioRequest(
                "Ana Souza",
                "ana@email.com",
                "12345678900",
                "999999999",
                "senha123",
                PerfilUsuario.CIDADAO
        );
        when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hash-senha");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Usuario resultado = usuarioService.cadastrar(request);

        assertEquals("Ana Souza", resultado.getNome());
        assertEquals("ana@email.com", resultado.getEmail());
        assertEquals("hash-senha", resultado.getSenhaHash());
        assertEquals(PerfilUsuario.CIDADAO, resultado.getPerfil());
        verify(passwordEncoder).encode("senha123");
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Não deve cadastrar usuário quando o email já estiver em uso")
    void naoDeveCadastrarUsuarioQuandoEmailJaEstiverEmUso() {
        UsuarioRequest request = new UsuarioRequest(
                "Ana Souza",
                "ana@email.com",
                "12345678900",
                "999999999",
                "senha123",
                PerfilUsuario.CIDADAO
        );
        when(usuarioRepository.findByEmail("ana@email.com")).thenReturn(Optional.of(new Usuario()));

        IllegalArgumentException excecao = assertThrows(
                IllegalArgumentException.class,
                () -> usuarioService.cadastrar(request)
        );

        assertEquals("E-mail já cadastrado.", excecao.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }
}
