package com.aep.backend.infra.security;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar quando o token for válido no header Authorization")
    void deveAutenticarQuandoTokenForValidoNoHeaderAuthorization() throws Exception {
        UserDetails userDetails = User.withUsername("usuario@email.com")
                .password("senha")
                .authorities(Collections.emptyList())
                .build();
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        when(jwtTokenProvider.validateToken("token-valido")).thenReturn(true);
        when(jwtTokenProvider.getEmailFromToken("token-valido")).thenReturn("usuario@email.com");
        when(userDetailsService.loadUserByUsername("usuario@email.com")).thenReturn(userDetails);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o header Authorization estiver ausente")
    void naoDeveAutenticarQuandoHeaderAuthorizationEstiverAusente() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o header Authorization não usa o prefixo Bearer")
    void naoDeveAutenticarQuandoHeaderAuthorizationNaoUsaPrefixoBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic token-invalido");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve autenticar quando o token for inválido")
    void naoDeveAutenticarQuandoTokenForInvalido() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer token-invalido");
        when(jwtTokenProvider.validateToken("token-invalido")).thenReturn(false);
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Não deve substituir uma autenticação já existente no contexto de segurança")
    void naoDeveSubstituirAutenticacaoExistenteQuandoTokenForValido() throws Exception {
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                "usuario@email.com", null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(request.getHeader("Authorization")).thenReturn("Bearer token-valido");
        jwtAuthFilter.doFilterInternal(request, response, filterChain);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername("usuario@email.com");
        verify(filterChain).doFilter(request, response);
    }
}
