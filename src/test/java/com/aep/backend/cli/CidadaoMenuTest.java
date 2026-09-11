package com.aep.backend.cli;

import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.service.CategoriaService;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.dto.UsuarioRequest;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.service.UsuarioService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CidadaoMenuTest {

    @Mock
    private SolicitacaoService solicitacaoService;

    @Mock
    private CategoriaService categoriaService;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private AuthenticationManager authenticationManager;

    private final PrintStream saidaOriginal = System.out;
    private ByteArrayOutputStream saida;

    @BeforeEach
    void setUp() {
        saida = new ByteArrayOutputStream();
        System.setOut(new PrintStream(saida));
    }

    @AfterEach
    void tearDown() {
        System.setOut(saidaOriginal);
    }

    private CidadaoMenu criarMenu(String entrada) {
        Scanner scanner = new Scanner(entrada);
        return new CidadaoMenu(scanner, solicitacaoService, categoriaService, usuarioService, authenticationManager);
    }

    @Test
    @DisplayName("Deve abrir solicitacao identificada quando dados forem validos")
    void deveAbrirSolicitacaoIdentificadaQuandoDadosForemValidos() {
        Categoria categoria = new Categoria("Lixo Irregular", "Descricao");
        categoria.setId("cat-1");
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);
        Solicitacao salva = new Solicitacao();
        salva.setProtocolo("DEN-2026-00001");
        when(solicitacaoService.criar(any(SolicitacaoRequest.class), eq(usuario))).thenReturn(salva);

        CidadaoMenu menu = criarMenu("1\nana@email.com\nsenha123\n1\nCentro\n\n\nDescricao valida\n0\n");
        menu.exibir();

        verify(solicitacaoService).criar(any(SolicitacaoRequest.class), eq(usuario));
        assertTrue(saida.toString().contains("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Nao deve abrir solicitacao identificada quando credenciais forem invalidas")
    void naoDeveAbrirSolicitacaoIdentificadaQuandoCredenciaisForemInvalidas() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("credenciais invalidas"));

        CidadaoMenu menu = criarMenu("1\nana@email.com\nsenhaerrada\n0\n");
        menu.exibir();

        assertTrue(saida.toString().contains("E-mail ou senha inválidos."));
        verify(solicitacaoService, never()).criar(any(), any());
    }

    @Test
    @DisplayName("Nao deve abrir solicitacao quando a categoria informada for invalida")
    void naoDeveAbrirSolicitacaoQuandoCategoriaInformadaForInvalida() {
        Categoria categoria = new Categoria("Lixo Irregular", "Descricao");
        categoria.setId("cat-1");
        Usuario usuario = new Usuario();
        usuario.setId("user-1");
        Authentication authentication = org.mockito.Mockito.mock(Authentication.class);
        when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(usuario);

        CidadaoMenu menu = criarMenu("1\nana@email.com\nsenha123\n9\n0\n");
        menu.exibir();

        assertTrue(saida.toString().contains("Categoria inválida."));
        verify(solicitacaoService, never()).criar(any(), any());
    }

    @Test
    @DisplayName("Deve abrir solicitacao anonima quando descricao for valida")
    void deveAbrirSolicitacaoAnonimaQuandoDescricaoForValida() {
        Categoria categoria = new Categoria("Lixo Irregular", "Descricao");
        categoria.setId("cat-1");
        when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));
        Solicitacao salva = new Solicitacao();
        salva.setProtocolo("DEN-2026-00002");
        when(solicitacaoService.criar(any(SolicitacaoRequest.class), isNull())).thenReturn(salva);

        CidadaoMenu menu = criarMenu("2\n1\nCentro\n\n\nDescricao anonima com bastante caracteres para passar na validacao\n\n\n0\n");
        menu.exibir();

        verify(solicitacaoService).criar(any(SolicitacaoRequest.class), isNull());
        assertTrue(saida.toString().contains("DEN-2026-00002"));
        assertTrue(saida.toString().contains("Guarde o protocolo"));
    }

    @Test
    @DisplayName("Deve exibir erro ao abrir solicitacao anonima quando descricao for curta")
    void deveExibirErroAoAbrirSolicitacaoAnonimaQuandoDescricaoForCurta() {
        Categoria categoria = new Categoria("Lixo Irregular", "Descricao");
        categoria.setId("cat-1");
        when(categoriaService.listarAtivas()).thenReturn(List.of(categoria));
        when(solicitacaoService.criar(any(SolicitacaoRequest.class), isNull()))
                .thenThrow(new IllegalArgumentException("Descrição deve ter no mínimo 50 caracteres para denúncia anônima"));

        CidadaoMenu menu = criarMenu("2\n1\nCentro\n\n\nDescricao curta\n\n\n0\n");
        menu.exibir();

        assertTrue(saida.toString().contains("Erro: Descrição deve ter no mínimo 50 caracteres para denúncia anônima"));
    }

    @Test
    @DisplayName("Deve consultar protocolo quando ele existir")
    void deveConsultarProtocoloQuandoElaExistir() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-1");
        solicitacao.setProtocolo("DEN-2026-00001");
        solicitacao.setCategoria(new com.aep.backend.domain.categoria.entity.CategoriaResumo("cat-1", "Lixo Irregular"));
        solicitacao.setBairro("Centro");
        solicitacao.setDescricao("Descricao");
        solicitacao.setDataCadastro(java.time.LocalDateTime.now());
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-00001")).thenReturn(solicitacao);
        when(solicitacaoService.buscarHistorico("sol-1")).thenReturn(List.of());

        CidadaoMenu menu = criarMenu("3\nden-2026-00001\n0\n");
        menu.exibir();

        verify(solicitacaoService).buscarPorProtocolo("DEN-2026-00001");
        assertTrue(saida.toString().contains("DEN-2026-00001"));
    }

    @Test
    @DisplayName("Nao deve consultar protocolo quando ele nao existir")
    void naoDeveConsultarProtocoloQuandoElaNaoExistir() {
        when(solicitacaoService.buscarPorProtocolo("DEN-2026-99999"))
                .thenThrow(new IllegalArgumentException("Protocolo não encontrado: DEN-2026-99999"));

        CidadaoMenu menu = criarMenu("3\nden-2026-99999\n0\n");
        menu.exibir();

        assertTrue(saida.toString().contains("Erro: Protocolo não encontrado: DEN-2026-99999"));
    }

    @Test
    @DisplayName("Deve cadastrar cidadao quando dados forem validos")
    void deveCadastrarCidadaoQuandoDadosForemValidos() {
        when(usuarioService.cadastrar(any(UsuarioRequest.class))).thenReturn(new Usuario());

        CidadaoMenu menu = criarMenu("4\nAna\nana@email.com\n12345678900\n999999999\nsenha123\n0\n");
        menu.exibir();

        verify(usuarioService).cadastrar(argThat((UsuarioRequest req) -> req.perfil() == PerfilUsuario.CIDADAO && req.email().equals("ana@email.com")));
        assertTrue(saida.toString().contains("Cadastro realizado com sucesso."));
    }

    @Test
    @DisplayName("Nao deve cadastrar cidadao quando o email ja estiver cadastrado")
    void naoDeveCadastrarCidadaoQuandoEmailJaEstiverCadastrado() {
        when(usuarioService.cadastrar(any(UsuarioRequest.class)))
                .thenThrow(new IllegalArgumentException("E-mail já cadastrado."));

        CidadaoMenu menu = criarMenu("4\nAna\nana@email.com\n12345678900\n999999999\nsenha123\n0\n");
        menu.exibir();

        assertTrue(saida.toString().contains("Erro: E-mail já cadastrado."));
    }

    @Test
    @DisplayName("Deve encerrar o menu quando a opcao for zero")
    void deveEncerrarMenuQuandoOpcaoForZero() {
        CidadaoMenu menu = criarMenu("0\n");

        menu.exibir();

        verify(solicitacaoService, never()).criar(any(), any());
    }

    @Test
    @DisplayName("Deve exibir mensagem de opcao invalida quando a opcao nao existir")
    void deveExibirMensagemDeOpcaoInvalidaQuandoOpcaoNaoExistir() {
        CidadaoMenu menu = criarMenu("9\n0\n");

        menu.exibir();

        assertTrue(saida.toString().contains("Opção inválida."));
    }
}
