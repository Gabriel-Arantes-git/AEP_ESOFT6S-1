package com.aep.backend.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusSolicitacaoTest {

    @Test
    @DisplayName("Deve conter exatamente os cinco status do fluxo, na ordem esperada")
    void deveConterExatamenteOsCincoStatusDoFluxoNaOrdemEsperada() {
        StatusSolicitacao[] status = StatusSolicitacao.values();

        assertEquals(5, status.length);
        assertEquals(StatusSolicitacao.ABERTO, status[0]);
        assertEquals(StatusSolicitacao.TRIAGEM, status[1]);
        assertEquals(StatusSolicitacao.EM_EXECUCAO, status[2]);
        assertEquals(StatusSolicitacao.RESOLVIDO, status[3]);
        assertEquals(StatusSolicitacao.ENCERRADO, status[4]);
    }

    @Test
    @DisplayName("ABERTO deve permitir mover apenas para TRIAGEM")
    void abertoDevePermitirMoverApenasParaTriagem() {
        assertTrue(StatusSolicitacao.ABERTO.podeMoverPara(StatusSolicitacao.TRIAGEM));
        assertFalse(StatusSolicitacao.ABERTO.podeMoverPara(StatusSolicitacao.EM_EXECUCAO));
        assertFalse(StatusSolicitacao.ABERTO.podeMoverPara(StatusSolicitacao.RESOLVIDO));
        assertFalse(StatusSolicitacao.ABERTO.podeMoverPara(StatusSolicitacao.ENCERRADO));
        assertFalse(StatusSolicitacao.ABERTO.podeMoverPara(StatusSolicitacao.ABERTO));
    }

    @Test
    @DisplayName("TRIAGEM deve permitir mover para EM_EXECUCAO ou ENCERRADO")
    void triagemDevePermitirMoverParaEmExecucaoOuEncerrado() {
        assertTrue(StatusSolicitacao.TRIAGEM.podeMoverPara(StatusSolicitacao.EM_EXECUCAO));
        assertTrue(StatusSolicitacao.TRIAGEM.podeMoverPara(StatusSolicitacao.ENCERRADO));
        assertFalse(StatusSolicitacao.TRIAGEM.podeMoverPara(StatusSolicitacao.ABERTO));
        assertFalse(StatusSolicitacao.TRIAGEM.podeMoverPara(StatusSolicitacao.RESOLVIDO));
        assertFalse(StatusSolicitacao.TRIAGEM.podeMoverPara(StatusSolicitacao.TRIAGEM));
    }

    @Test
    @DisplayName("EM_EXECUCAO deve permitir mover apenas para RESOLVIDO")
    void emExecucaoDevePermitirMoverApenasParaResolvido() {
        assertTrue(StatusSolicitacao.EM_EXECUCAO.podeMoverPara(StatusSolicitacao.RESOLVIDO));
        assertFalse(StatusSolicitacao.EM_EXECUCAO.podeMoverPara(StatusSolicitacao.ABERTO));
        assertFalse(StatusSolicitacao.EM_EXECUCAO.podeMoverPara(StatusSolicitacao.TRIAGEM));
        assertFalse(StatusSolicitacao.EM_EXECUCAO.podeMoverPara(StatusSolicitacao.ENCERRADO));
        assertFalse(StatusSolicitacao.EM_EXECUCAO.podeMoverPara(StatusSolicitacao.EM_EXECUCAO));
    }

    @Test
    @DisplayName("RESOLVIDO deve permitir mover apenas para ENCERRADO")
    void resolvidoDevePermitirMoverApenasParaEncerrado() {
        assertTrue(StatusSolicitacao.RESOLVIDO.podeMoverPara(StatusSolicitacao.ENCERRADO));
        assertFalse(StatusSolicitacao.RESOLVIDO.podeMoverPara(StatusSolicitacao.ABERTO));
        assertFalse(StatusSolicitacao.RESOLVIDO.podeMoverPara(StatusSolicitacao.TRIAGEM));
        assertFalse(StatusSolicitacao.RESOLVIDO.podeMoverPara(StatusSolicitacao.EM_EXECUCAO));
        assertFalse(StatusSolicitacao.RESOLVIDO.podeMoverPara(StatusSolicitacao.RESOLVIDO));
    }

    @ParameterizedTest
    @DisplayName("ENCERRADO nao deve permitir mover para nenhum status")
    @EnumSource(StatusSolicitacao.class)
    void encerradoNaoDevePermitirMoverParaNenhumStatus(StatusSolicitacao proximo) {
        assertFalse(StatusSolicitacao.ENCERRADO.podeMoverPara(proximo));
    }
}
