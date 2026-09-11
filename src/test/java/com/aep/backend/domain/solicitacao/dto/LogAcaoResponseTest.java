package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.usuario.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LogAcaoResponseTest {

    @Test
    @DisplayName("Deve criar o log acao response quando o log tiver usuario")
    void deveCriarLogAcaoResponseQuandoLogTiverUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("Ana");
        usuario.setPerfil(PerfilUsuario.GESTOR);
        LogAcao log = new LogAcao(usuario, "MOVER_STATUS", "solicitacao", "sol-1", "ABERTO -> TRIAGEM");
        log.setId("log-1");

        LogAcaoResponse response = LogAcaoResponse.from(log);

        assertEquals("log-1", response.id());
        assertEquals("MOVER_STATUS", response.acao());
        assertEquals("sol-1", response.entidadeId());
        assertEquals("Ana", response.usuarioNome());
    }

    @Test
    @DisplayName("Deve criar o log acao response com usuario nome nulo quando o log nao tiver usuario")
    void deveCriarLogAcaoResponseComUsuarioNomeNuloQuandoLogNaoTiverUsuario() {
        LogAcao log = new LogAcao(null, "ABRIR_SOLICITACAO", "solicitacao", "sol-2", "Solicitação aberta.");
        log.setId("log-2");

        LogAcaoResponse response = LogAcaoResponse.from(log);

        assertNull(response.usuarioNome());
    }
}
