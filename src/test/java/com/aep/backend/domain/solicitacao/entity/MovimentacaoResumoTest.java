package com.aep.backend.domain.solicitacao.entity;

import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MovimentacaoResumoTest {

    @Test
    @DisplayName("Deve criar o resumo com os dados da movimentacao")
    void deveCriarResumoComOsDadosDaMovimentacao() {
        LocalDateTime data = LocalDateTime.now();
        UsuarioResumo responsavel = new UsuarioResumo("user-1", "Atendente");
        Movimentacao movimentacao = new Movimentacao();
        movimentacao.setId("mov-1");
        movimentacao.setSolicitacaoId("sol-1");
        movimentacao.setStatusAnterior(StatusSolicitacao.ABERTO);
        movimentacao.setStatusNovo(StatusSolicitacao.TRIAGEM);
        movimentacao.setComentario("Encaminhada para triagem");
        movimentacao.setResponsavel(responsavel);
        movimentacao.setJustificativaAtraso("Aguardando peça");
        movimentacao.setDataCadastro(data);

        MovimentacaoResumo resumo = MovimentacaoResumo.from(movimentacao);

        assertEquals("mov-1", resumo.id());
        assertEquals(StatusSolicitacao.ABERTO, resumo.statusAnterior());
        assertEquals(StatusSolicitacao.TRIAGEM, resumo.statusNovo());
        assertEquals("Encaminhada para triagem", resumo.comentario());
        assertEquals(responsavel, resumo.responsavel());
        assertEquals("Aguardando peça", resumo.justificativaAtraso());
        assertEquals(data, resumo.dataMovimentacao());
    }
}
