package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MovimentacaoResponseTest {

    @Test
    @DisplayName("Deve criar a movimentacao response quando o status anterior existir")
    void deveCriarMovimentacaoResponseQuandoStatusAnteriorExistir() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId("mov-1");
        movimentacao.setStatusAnterior(StatusSolicitacao.ABERTO);
        movimentacao.setStatusNovo(StatusSolicitacao.TRIAGEM);
        movimentacao.setComentario("Encaminhado");
        movimentacao.setResponsavel(new UsuarioResumo("user-1", "Ana"));

        MovimentacaoResponse response = MovimentacaoResponse.from(movimentacao);

        assertEquals("mov-1", response.id());
        assertEquals("ABERTO", response.statusAnterior());
        assertEquals("TRIAGEM", response.statusNovo());
        assertEquals("Ana", response.responsavelNome());
    }

    @Test
    @DisplayName("Deve criar a movimentacao response com status anterior e responsavel nulos quando ausentes")
    void deveCriarMovimentacaoResponseComStatusAnteriorEResponsavelNulosQuandoAusentes() {
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId("mov-2");
        movimentacao.setStatusNovo(StatusSolicitacao.ABERTO);
        movimentacao.setComentario("Solicitação aberta.");

        MovimentacaoResponse response = MovimentacaoResponse.from(movimentacao);

        assertNull(response.statusAnterior());
        assertNull(response.responsavelNome());
    }
}
