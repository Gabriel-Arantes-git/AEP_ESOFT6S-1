package com.aep.backend.infra.config;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DataInitializerTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private DepartamentoRepository departamentoRepository;

    @Mock
    private SlaRepository slaRepository;

    @InjectMocks
    private DataInitializer dataInitializer;

    @Test
    @DisplayName("Deve inicializar usuários, categorias, departamentos e SLA quando os registros ainda não existirem")
    void deveInicializarUsuariosCategoriasDepartamentosESlaQuandoRegistrosNaoExistirem() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> "encoded-" + invocation.getArgument(0));
        when(categoriaRepository.existsByNome(anyString())).thenReturn(false);
        when(departamentoRepository.existsByNome(anyString())).thenReturn(false);
        when(slaRepository.existsByPrioridade(any())).thenReturn(false);

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(usuarioRepository, times(3)).save(any(Usuario.class));
        verify(categoriaRepository, atLeastOnce()).save(any(Categoria.class));
        verify(departamentoRepository, atLeastOnce()).save(any(DepartamentoDestino.class));
        verify(slaRepository, atLeastOnce()).save(any(SlaConfig.class));
    }

    @Test
    @DisplayName("Não deve inicializar registros que já existem no banco")
    void naoDeveInicializarRegistrosQueJaExistem() {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(new Usuario()));
        when(categoriaRepository.existsByNome(anyString())).thenReturn(true);
        when(departamentoRepository.existsByNome(anyString())).thenReturn(true);
        when(slaRepository.existsByPrioridade(any())).thenReturn(true);

        dataInitializer.run(new DefaultApplicationArguments(new String[0]));

        verify(usuarioRepository, never()).save(any(Usuario.class));
        verify(categoriaRepository, never()).save(any(Categoria.class));
        verify(departamentoRepository, never()).save(any(DepartamentoDestino.class));
        verify(slaRepository, never()).save(any(SlaConfig.class));
    }
}
