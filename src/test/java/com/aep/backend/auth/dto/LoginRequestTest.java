package com.aep.backend.auth.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar o login request com email e senha válidos")
    void deveCriarLoginRequestQuandoDadosForemValidos() {
        LoginRequest request = new LoginRequest("ana@email.com", "senha123");

        assertEquals("ana@email.com", request.email());
        assertEquals("senha123", request.senha());
    }

    @Test
    @DisplayName("Não deve validar quando o email estiver em branco")
    void naoDeveValidarQuandoEmailEstiverEmBranco() {
        LoginRequest request = new LoginRequest("", "senha123");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(violation -> "email".equals(violation.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Não deve validar quando a senha estiver em branco")
    void naoDeveValidarQuandoSenhaEstiverEmBranco() {
        LoginRequest request = new LoginRequest("ana@email.com", "");

        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(violation -> "senha".equals(violation.getPropertyPath().toString())));
    }
}
