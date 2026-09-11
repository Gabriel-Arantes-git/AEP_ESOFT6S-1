package com.aep.backend.domain.usuario.dto;

import com.aep.backend.domain.enums.PerfilUsuario;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UsuarioRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar o usuario request quando os dados forem validos")
    void deveCriarUsuarioRequestQuandoDadosForemValidos() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "12345678900", "999999999", "senha123", PerfilUsuario.CIDADAO);

        assertTrue(validator.validate(request).isEmpty());
        assertEquals("Ana", request.nome());
        assertEquals(PerfilUsuario.CIDADAO, request.perfil());
    }

    @Test
    @DisplayName("Nao deve validar quando o nome estiver em branco")
    void naoDeveValidarQuandoNomeEstiverEmBranco() {
        UsuarioRequest request = new UsuarioRequest("", "ana@email.com", "12345678900", "999999999", "senha123", PerfilUsuario.CIDADAO);

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "nome".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando o email for invalido")
    void naoDeveValidarQuandoEmailForInvalido() {
        UsuarioRequest request = new UsuarioRequest("Ana", "email-invalido", "12345678900", "999999999", "senha123", PerfilUsuario.CIDADAO);

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "email".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando o cpf estiver em branco")
    void naoDeveValidarQuandoCpfEstiverEmBranco() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "", "999999999", "senha123", PerfilUsuario.CIDADAO);

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "cpf".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando a senha estiver em branco")
    void naoDeveValidarQuandoSenhaEstiverEmBranco() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "12345678900", "999999999", "", PerfilUsuario.CIDADAO);

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "senha".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando o perfil for nulo")
    void naoDeveValidarQuandoPerfilForNulo() {
        UsuarioRequest request = new UsuarioRequest("Ana", "ana@email.com", "12345678900", "999999999", "senha123", null);

        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "perfil".equals(v.getPropertyPath().toString())));
    }
}
