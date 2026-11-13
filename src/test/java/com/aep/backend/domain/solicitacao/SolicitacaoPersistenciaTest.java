package com.aep.backend.domain.solicitacao;

import com.aep.backend.MongoIntegrationTest;
import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest;
import com.aep.backend.domain.solicitacao.entity.Localizacao;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.repository.MovimentacaoRepository;
import com.aep.backend.domain.solicitacao.repository.SolicitacaoRepository;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SolicitacaoPersistenciaTest extends MongoIntegrationTest {

    @Autowired
    private SolicitacaoService solicitacaoService;

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private MovimentacaoRepository movimentacaoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void limparSolicitacoes() {
        mongoTemplate.remove(new Query(), Solicitacao.class);
        mongoTemplate.remove(new Query(), Movimentacao.class);
    }

    @Test
    @DisplayName("Deve gravar a localizacao como subdocumento aninhado na solicitacao")
    void deveGravarLocalizacaoComoSubdocumentoAninhado() {
        Solicitacao solicitacao = solicitacaoService.criar(requestValido(), cidadao());

        Document documento = buscarDocumento(solicitacao.getId());
        Document localizacao = documento.get("localizacao", Document.class);

        assertNotNull(localizacao);
        assertEquals("Rua das Flores", localizacao.getString("logradouro"));
        assertEquals("Centro", localizacao.getString("bairro"));
        assertEquals("01000-000", localizacao.getString("cep"));
        assertEquals("Próximo ao mercado", localizacao.getString("referencia"));
        assertEquals(-23.56, localizacao.getDouble("latitude"));
        assertEquals(-46.67, localizacao.getDouble("longitude"));
        assertFalse(documento.containsKey("bairro"));
        assertFalse(documento.containsKey("latitude"));
    }

    @Test
    @DisplayName("Deve gravar o historico como lista de subdocumentos a cada movimentacao")
    void deveGravarHistoricoComoListaDeSubdocumentos() {
        Solicitacao solicitacao = solicitacaoService.criar(requestValido(), cidadao());

        List<Document> historicoAbertura = buscarDocumento(solicitacao.getId()).getList("historico", Document.class);
        assertEquals(1, historicoAbertura.size());
        assertEquals("ABERTO", historicoAbertura.get(0).getString("statusNovo"));

        solicitacaoService.moverStatus(solicitacao.getId(), triagem(), atendente());

        List<Document> historico = buscarDocumento(solicitacao.getId()).getList("historico", Document.class);
        assertEquals(2, historico.size());
        assertEquals("ABERTO", historico.get(1).getString("statusAnterior"));
        assertEquals("TRIAGEM", historico.get(1).getString("statusNovo"));
        assertEquals("Encaminhada para triagem", historico.get(1).getString("comentario"));
        assertEquals("Atendente", historico.get(1).get("responsavel", Document.class).getString("nome"));
        assertNotNull(historico.get(1).getDate("dataMovimentacao"));
    }

    @Test
    @DisplayName("Deve relacionar solicitacao, movimentacao, usuario, categoria e departamento por id")
    void deveRelacionarColecoesPorId() {
        Usuario cidadao = cidadao();
        Solicitacao solicitacao = solicitacaoService.criar(requestValido(), cidadao);
        solicitacaoService.moverStatus(solicitacao.getId(), triagem(), atendente());

        Solicitacao salva = solicitacaoRepository.findById(solicitacao.getId()).orElseThrow();
        assertEquals(cidadao.getId(), salva.getUsuarioId());
        assertTrue(usuarioRepository.findById(salva.getUsuarioId()).isPresent());
        assertTrue(categoriaRepository.findById(salva.getCategoria().id()).isPresent());
        assertTrue(departamentoRepository.findById(salva.getDepartamento().id()).isPresent());

        List<Movimentacao> movimentacoes = movimentacaoRepository
                .findAllBySolicitacaoIdOrderByDataCadastroAsc(solicitacao.getId());
        assertEquals(2, movimentacoes.size());
        assertTrue(movimentacoes.stream().allMatch(m -> solicitacao.getId().equals(m.getSolicitacaoId())));

        assertEquals(movimentacoes.get(0).getId(), salva.getHistorico().get(0).id());
        assertEquals(movimentacoes.get(1).getId(), salva.getHistorico().get(1).id());
    }

    private Document buscarDocumento(String id) {
        return mongoTemplate.findById(id, Document.class, "solicitacao");
    }

    private SolicitacaoRequest requestValido() {
        return new SolicitacaoRequest(
                categoria("Lixo Irregular").getId(),
                "Descarte irregular de lixo na calçada há mais de uma semana.",
                new Localizacao("Rua das Flores", "Centro", "01000-000", "Próximo ao mercado", -23.56, -46.67),
                false,
                null,
                null
        );
    }

    private MoverStatusRequest triagem() {
        return new MoverStatusRequest(
                StatusSolicitacao.TRIAGEM,
                "Encaminhada para triagem",
                Prioridade.ALTA,
                departamento("Prefeitura Municipal").getId()
        );
    }

    private Usuario cidadao() {
        return usuarioRepository.findByEmail("teste123@gmail.com").orElseThrow();
    }

    private Usuario atendente() {
        return usuarioRepository.findByEmail("atendente@gov").orElseThrow();
    }

    private Categoria categoria(String nome) {
        return categoriaRepository.findAllByAtivoTrue().stream()
                .filter(c -> nome.equals(c.getNome()))
                .findFirst()
                .orElseThrow();
    }

    private DepartamentoDestino departamento(String nome) {
        return departamentoRepository.findAllByAtivoTrue().stream()
                .filter(d -> nome.equals(d.getNome()))
                .findFirst()
                .orElseThrow();
    }
}
