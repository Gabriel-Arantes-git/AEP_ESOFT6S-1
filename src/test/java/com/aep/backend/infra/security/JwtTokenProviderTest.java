package com.aep.backend.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI=");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 3600000L);
    }

    @Test
    @DisplayName("Deve gerar e validar token quando o email for válido")
    void deveGerarEValidarTokenQuandoEmailForValido() {
        String token = jwtTokenProvider.generateToken("usuario@email.com");

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals("usuario@email.com", jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    @DisplayName("Não deve validar quando o token estiver malformado")
    void naoDeveValidarQuandoTokenEstiverMalformado() {
        assertFalse(jwtTokenProvider.validateToken("token-invalido"));
    }

    @Test
    @DisplayName("Não deve validar quando o token estiver expirado")
    void naoDeveValidarQuandoTokenEstiverExpirado() {
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", -1000L);
        String tokenExpirado = jwtTokenProvider.generateToken("usuario@email.com");

        assertFalse(jwtTokenProvider.validateToken(tokenExpirado));
    }
}
