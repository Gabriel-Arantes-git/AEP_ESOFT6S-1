package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MoverStatusRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Deve criar o mover status request quando os dados forem validos")
    void deveCriarMoverStatusRequestQuandoDadosForemValidos() {
        MoverStatusRequest request = new MoverStatusRequest(StatusSolicitacao.TRIAGEM, "Comentario", Prioridade.ALTA, "dep-1");

        assertTrue(validator.validate(request).isEmpty());
        assertEquals(StatusSolicitacao.TRIAGEM, request.novoStatus());
        assertEquals("dep-1", request.departamentoId());
    }

    @Test
    @DisplayName("Nao deve validar quando o novo status for nulo")
    void naoDeveValidarQuandoNovoStatusForNulo() {
        MoverStatusRequest request = new MoverStatusRequest(null, "Comentario", Prioridade.ALTA, "dep-1");

        Set<ConstraintViolation<MoverStatusRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "novoStatus".equals(v.getPropertyPath().toString())));
    }

    @Test
    @DisplayName("Nao deve validar quando o comentario estiver em branco")
    void naoDeveValidarQuandoComentarioEstiverEmBranco() {
        MoverStatusRequest request = new MoverStatusRequest(StatusSolicitacao.TRIAGEM, "", Prioridade.ALTA, "dep-1");

        Set<ConstraintViolation<MoverStatusRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> "comentario".equals(v.getPropertyPath().toString())));
    }
}
