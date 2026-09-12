package com.aep.backend.auth;

import com.aep.backend.auth.dto.LoginRequest;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.infra.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .addFilters(new com.aep.backend.TestSecurityFilter())
                .build();
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar token JWT quando os dados forem válidos")
    void deveAutenticarUsuarioERetornarTokenQuandoDadosForemValidos() throws Exception {
        LoginRequest request = new LoginRequest("ana@email.com", "senha123");
        Usuario usuario = new Usuario();
        usuario.setEmail("ana@email.com");
        usuario.setNome("Ana");
        usuario.setPerfil(PerfilUsuario.CIDADAO);

        Authentication authentication = mock(Authentication.class);
        org.mockito.Mockito.lenient().when(authenticationManager.authenticate(any())).thenReturn(authentication);
        org.mockito.Mockito.lenient().when(authentication.getPrincipal()).thenReturn(usuario);
        org.mockito.Mockito.lenient().when(jwtTokenProvider.generateToken("ana@email.com")).thenReturn("jwt-token");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"ana@email.com\",\"senha\":\"senha123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.email").value("ana@email.com"))
                .andExpect(jsonPath("$.nome").value("Ana"))
                .andExpect(jsonPath("$.perfil").value("CIDADAO"));

        verify(authenticationManager).authenticate(any());
        verify(jwtTokenProvider).generateToken("ana@email.com");
    }

    @Test
    @DisplayName("Não deve autenticar quando o payload estiver inválido")
    void naoDeveAutenticarQuandoPayloadForInvalido() throws Exception {
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(jwtTokenProvider);
        verifyNoInteractions(authenticationManager);
    }
}
