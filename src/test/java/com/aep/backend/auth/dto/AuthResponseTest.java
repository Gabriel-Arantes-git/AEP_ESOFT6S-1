package com.aep.backend.auth.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthResponseTest {

    @Test
    @DisplayName("Deve criar a resposta de autenticação com os dados esperados")
    void deveCriarRespostaQuandoDadosForemValidos() {
        AuthResponse response = new AuthResponse("jwt-token", "ana@email.com", "Ana", "CIDADAO");

        assertEquals("jwt-token", response.token());
        assertEquals("ana@email.com", response.email());
        assertEquals("Ana", response.nome());
        assertEquals("CIDADAO", response.perfil());
    }
}
