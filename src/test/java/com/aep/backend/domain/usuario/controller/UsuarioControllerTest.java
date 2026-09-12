package com.aep.backend.domain.usuario.controller;

import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.service.UsuarioService;
import com.aep.backend.infra.exception.GlobalExceptionHandler;
import com.aep.backend.infra.security.JwtTokenProvider;
import com.aep.backend.infra.security.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith({MockitoExtension.class, SpringExtension.class})
class UsuarioControllerTest {

    private UsuarioController usuarioController;

    private MockMvc mockMvc;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsServiceImpl;

    @Mock
    private MongoMappingContext mongoMappingContext;

    @BeforeEach
    void setUp() {
        usuarioController = new UsuarioController(usuarioService);

        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    private String usuarioValidoJson() {
        return """
                {"nome":"Ana","email":"ana@email.com","cpf":"12345678900",
                 "telefone":"11999999999","senha":"senha123","perfil":"CIDADAO"}
                """;
    }

    @Test
    @DisplayName("Deve retornar status 201 ao cadastrar um usuario com dados validos")
    void deveRetornarStatus201AoCadastrarUsuarioComDadosValidos() throws Exception {
        Usuario salvo = new Usuario();
        salvo.setId("user-1");
        salvo.setNome("Ana");
        salvo.setEmail("ana@email.com");
        salvo.setCpf("12345678900");
        salvo.setPerfil(PerfilUsuario.CIDADAO);
        org.mockito.Mockito.lenient().when(usuarioService.cadastrar(any())).thenReturn(salvo);

        mockMvc.perform(post("/usuarios/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioValidoJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.email").value("ana@email.com"));
    }

    @Test
    @DisplayName("Nao deve cadastrar usuario quando o payload estiver invalido")
    void naoDeveCadastrarUsuarioQuandoPayloadForInvalido() throws Exception {
        mockMvc.perform(post("/usuarios/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":\"\",\"email\":\"invalido\",\"cpf\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve propagar erro 400 quando o e-mail ja estiver cadastrado")
    void devePropagarErro400QuandoEmailJaEstiverCadastrado() throws Exception {
        org.mockito.Mockito.lenient().when(usuarioService.cadastrar(any()))
                .thenThrow(new IllegalArgumentException("E-mail já cadastrado."));

        mockMvc.perform(post("/usuarios/cadastrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioValidoJson()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensagem").value("E-mail já cadastrado."));
    }

    @Test
    @DisplayName("Deve retornar status 201 ao criar usuario administrativamente quando o perfil e GESTOR")
    @WithMockUser(roles = "GESTOR")
    void deveRetornarStatus201AoCriarUsuarioAdministrativamenteQuandoPerfilEGestor() throws Exception {
        Usuario salvo = new Usuario();
        salvo.setId("user-1");
        salvo.setNome("Ana");
        salvo.setEmail("ana@email.com");
        salvo.setCpf("12345678900");
        salvo.setSenhaHash("hash");
        salvo.setPerfil(PerfilUsuario.ATENDENTE);
        org.mockito.Mockito.lenient().when(usuarioService.salvar(any(Usuario.class))).thenReturn(salvo);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","cpf":"12345678900",
                                 "senhaHash":"hash","perfil":"ATENDENTE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("user-1"));
    }

    @Test
    @DisplayName("Deve retornar status 403 ao criar usuario administrativamente quando o perfil nao e GESTOR")
    @WithMockUser(roles = "ATENDENTE")
    void deveRetornarStatus403AoCriarUsuarioAdministrativamenteQuandoPerfilNaoEGestor() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","cpf":"12345678900",
                                 "senhaHash":"hash","perfil":"ATENDENTE"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 403 ao criar usuario administrativamente sem autenticacao")
    void deveRetornarStatus403AoCriarUsuarioAdministrativamenteSemAutenticacao() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","email":"ana@email.com","cpf":"12345678900",
                                 "senhaHash":"hash","perfil":"ATENDENTE"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 204 e delegar a inativacao quando o perfil e GESTOR")
    @WithMockUser(roles = "GESTOR")
    void deveRetornarStatus204EDelegarInativacaoQuandoPerfilEGestor() throws Exception {
        mockMvc.perform(delete("/usuarios/user-1"))
                .andExpect(status().isNoContent());

        verify(usuarioService).deletar("user-1");
    }

    @Test
    @DisplayName("Deve retornar status 403 ao inativar usuario quando o perfil nao e GESTOR")
    @WithMockUser(roles = "CIDADAO")
    void deveRetornarStatus403AoInativarUsuarioQuandoPerfilNaoEGestor() throws Exception {
        mockMvc.perform(delete("/usuarios/user-1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Deve retornar status 403 ao inativar usuario sem autenticacao")
    void deveRetornarStatus403AoInativarUsuarioSemAutenticacao() throws Exception {
        mockMvc.perform(delete("/usuarios/user-1"))
                .andExpect(status().isForbidden());
    }
}