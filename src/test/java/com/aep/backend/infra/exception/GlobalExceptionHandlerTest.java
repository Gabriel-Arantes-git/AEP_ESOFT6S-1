package com.aep.backend.infra.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve retornar 400 quando IllegalArgumentException for lançada")
    void deveRetornar400QuandoIllegalArgumentExceptionForLancada() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resposta =
                handler.handleBadRequest(new IllegalArgumentException("mensagem de erro"));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals("mensagem de erro", resposta.getBody().mensagem());
    }

    @Test
    @DisplayName("Deve retornar 409 quando IllegalStateException for lançada")
    void deveRetornar409QuandoIllegalStateExceptionForLancada() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resposta =
                handler.handleConflict(new IllegalStateException("conflito de estado"));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals("conflito de estado", resposta.getBody().mensagem());
    }

    @Test
    @DisplayName("Deve retornar 404 quando NoSuchElementException for lançada")
    void deveRetornar404QuandoNoSuchElementExceptionForLancada() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resposta =
                handler.handleNotFound(new NoSuchElementException("registro não encontrado"));

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
    }

    @Test
    @DisplayName("Deve retornar 409 quando DuplicateKeyException for lançada")
    void deveRetornar409QuandoDuplicateKeyExceptionForLancada() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resposta =
                handler.handleDuplicateKey(new DuplicateKeyException("duplicado"));

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertTrue(resposta.getBody().mensagem().contains("índice único"));
    }

    @Test
    @DisplayName("Deve concatenar erros de validação em uma mensagem única")
    void deveConcatenarErrosDeValidacaoEmUmaMensagemUnica() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(
                new FieldError("objeto", "email", "não pode ser vazio"),
                new FieldError("objeto", "senha", "não pode ser vazio")
        ));

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> resposta = handler.handleValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertTrue(resposta.getBody().mensagem().contains("email"));
        assertTrue(resposta.getBody().mensagem().contains("senha"));
    }
}
