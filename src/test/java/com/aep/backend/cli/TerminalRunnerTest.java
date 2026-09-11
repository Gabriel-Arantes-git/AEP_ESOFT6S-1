package com.aep.backend.cli;

import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.domain.departamento.service.DepartamentoService;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminalRunnerTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private DepartamentoService departamentoService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private TerminalRunner terminalRunner;

    private final InputStream entradaOriginal = System.in;
    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void tearDown() {
        System.setIn(entradaOriginal);
        System.setOut(saidaOriginal);
    }

    private void simularEntrada(String entrada) {
        System.setIn(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)));
    }

    private Usuario criarUsuario(PerfilUsuario perfil) {
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        usuario.setNome("Usuario Teste");
        usuario.setPerfil(perfil);
        return usuario;
    }

    @Test
    @DisplayName("Deve encerrar quando a opcao for zero")
    void deveEncerrarQuandoOpcaoForZero() {
        simularEntrada("0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("Encerrando."));
    }

    @Test
    @DisplayName("Deve exibir opcao invalida quando a opcao for desconhecida")
    void deveExibirOpcaoInvalidaQuandoOpcaoForDesconhecida() {
        simularEntrada("9\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("Opção inválida."));
    }

    @Test
    @DisplayName("Deve rotear para o menu cidadao quando a opcao for um")
    void deveRotearParaMenuCidadaoQuandoOpcaoForUm() {
        simularEntrada("1\n0\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("--- Cidadão ---"));
    }

    @Test
    @DisplayName("Deve exibir erro quando as credenciais forem invalidas")
    void deveExibirErroQuandoCredenciaisForemInvalidas() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("credenciais invalidas"));
        simularEntrada("2\na@a.com\nsenha\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("E-mail ou senha inválidos."));
    }

    @Test
    @DisplayName("Deve rotear para o menu gestor quando o perfil for gestor")
    void deveRotearParaMenuGestorQuandoPerfilForGestor() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(criarUsuario(PerfilUsuario.GESTOR));
        simularEntrada("2\nadmin@gov\nadmin\n0\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("--- Gestor:"));
    }

    @Test
    @DisplayName("Deve rotear para o menu atendente quando o perfil for atendente")
    void deveRotearParaMenuAtendenteQuandoPerfilForAtendente() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(criarUsuario(PerfilUsuario.ATENDENTE));
        simularEntrada("2\natendente@gov\n123\n0\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("--- Atendente:"));
    }

    @Test
    @DisplayName("Deve negar acesso quando o perfil for cidadao")
    void deveNegarAcessoQuandoPerfilForCidadao() {
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(criarUsuario(PerfilUsuario.CIDADAO));
        simularEntrada("2\nteste@gmail.com\nteste\n0\n");

        terminalRunner.run(null);

        assertTrue(saida.toString().contains("Acesso negado. Use o menu Cidadão."));
    }
}
