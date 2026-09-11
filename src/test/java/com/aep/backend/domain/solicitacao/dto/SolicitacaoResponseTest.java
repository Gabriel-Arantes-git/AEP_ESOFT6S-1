package com.aep.backend.domain.solicitacao.dto;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.departamento.entity.DepartamentoResumo;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SolicitacaoResponseTest {

    @Test
    @DisplayName("Deve criar a solicitacao response quando todos os campos estiverem preenchidos")
    void deveCriarSolicitacaoResponseQuandoTodosOsCamposEstiveremPreenchidos() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-1");
        solicitacao.setProtocolo("DEN-2026-00001");
        solicitacao.setStatus(StatusSolicitacao.TRIAGEM);
        solicitacao.setPrioridade(Prioridade.ALTA);
        solicitacao.setCategoria(new CategoriaResumo("cat-1", "Lixo Irregular"));
        solicitacao.setDepartamento(new DepartamentoResumo("dep-1", "Prefeitura"));
        solicitacao.setAtendente(new UsuarioResumo("user-1", "Ana"));
        solicitacao.setBairro("Centro");
        solicitacao.setDescricao("Descricao");
        solicitacao.setAnonimo(false);

        SolicitacaoResponse response = SolicitacaoResponse.from(solicitacao);

        assertEquals("sol-1", response.id());
        assertEquals("DEN-2026-00001", response.protocolo());
        assertEquals("TRIAGEM", response.status());
        assertEquals("ALTA", response.prioridade());
        assertEquals("cat-1", response.categoria().id());
        assertEquals("dep-1", response.departamento().id());
        assertEquals("user-1", response.atendente().id());
    }

    @Test
    @DisplayName("Deve criar a solicitacao response com departamento e atendente nulos quando ausentes")
    void deveCriarSolicitacaoResponseComDepartamentoEAtendenteNulosQuandoAusentes() {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setId("sol-2");
        solicitacao.setProtocolo("DEN-2026-00002");
        solicitacao.setStatus(StatusSolicitacao.ABERTO);
        solicitacao.setCategoria(new CategoriaResumo("cat-1", "Lixo Irregular"));
        solicitacao.setBairro("Centro");
        solicitacao.setDescricao("Descricao");
        solicitacao.setAnonimo(true);

        SolicitacaoResponse response = SolicitacaoResponse.from(solicitacao);

        assertNull(response.prioridade());
        assertNull(response.departamento());
        assertNull(response.atendente());
    }
}
