package com.aep.backend.domain.solicitacao.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar a solicitacao request quando os dados forem validos")
    void deveCriarSolicitacaoRequestQuandoDadosForemValidos() {
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-1", "Descricao valida", "Centro", "Rua A", "Referencia",
                false, "Ana", "ana@email.com", -25.0, -50.0, "80000000");

        assertTrue(validator.validate(request).isEmpty());
        assertEquals("cat-1", request.categoriaId());
        assertEquals("Centro", request.bairro());
    }

    @Test
    @DisplayName("Nao deve validar quando a categoriaId for nula")
    void naoDeveValidarQuandoCategoriaIdForNula() {
        SolicitacaoRequest request = new SolicitacaoRequest(
                null, "Descricao valida", "Centro", "Rua A", "Referencia",
                false, "Ana", "ana@email.com", -25.0, -50.0, "80000000");

        Set<ConstraintViolation<SolicitacaoRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "categoriaId".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando a descricao estiver em branco")
    void naoDeveValidarQuandoDescricaoEstiverEmBranco() {
        SolicitacaoRequest request = new SolicitacaoRequest(
                "cat-1", "", "Centro", "Rua A", "Referencia",
                false, "Ana", "ana@email.com", -25.0, -50.0, "80000000");

        Set<ConstraintViolation<SolicitacaoRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "descricao".equals(v.getPropertyPath().toString())));
    }
}
