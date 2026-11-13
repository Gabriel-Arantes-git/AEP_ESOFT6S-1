package com.aep.backend.infra.config;

import com.aep.backend.MongoIntegrationTest;
import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.repository.SlaRepository;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.repository.SolicitacaoRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import static org.junit.jupiter.api.Assertions.assertThrows;

class IndicesUnicosPersistenciaTest extends MongoIntegrationTest {

    @Autowired
    private SolicitacaoRepository solicitacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private SlaRepository slaRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void limparSolicitacoes() {
        mongoTemplate.remove(new Query(), Solicitacao.class);
    }

    @Test
    @DisplayName("Nao deve permitir duas solicitacoes com o mesmo protocolo")
    void naoDevePermitirProtocoloDuplicado() {
        solicitacaoRepository.save(solicitacao("DEN-2026-00001"));

        assertThrows(DuplicateKeyException.class,
                () -> solicitacaoRepository.save(solicitacao("DEN-2026-00001")));
    }

    @Test
    @DisplayName("Nao deve permitir dois usuarios com o mesmo email")
    void naoDevePermitirEmailDuplicado() {
        assertThrows(DuplicateKeyException.class,
                () -> usuarioRepository.save(usuario("admin@gov", "99999999901")));
    }

    @Test
    @DisplayName("Nao deve permitir dois usuarios com o mesmo cpf")
    void naoDevePermitirCpfDuplicado() {
        assertThrows(DuplicateKeyException.class,
                () -> usuarioRepository.save(usuario("novo@gov", "00000000000")));
    }

    @Test
    @DisplayName("Nao deve permitir duas categorias com o mesmo nome")
    void naoDevePermitirNomeDeCategoriaDuplicado() {
        assertThrows(DuplicateKeyException.class,
                () -> categoriaRepository.save(new Categoria("Queimada", "Duplicada")));
    }

    @Test
    @DisplayName("Nao deve permitir dois departamentos com o mesmo nome")
    void naoDevePermitirNomeDeDepartamentoDuplicado() {
        assertThrows(DuplicateKeyException.class,
                () -> departamentoRepository.save(new DepartamentoDestino("SEMA", "Duplicado")));
    }

    @Test
    @DisplayName("Nao deve permitir duas configuracoes de SLA para a mesma prioridade")
    void naoDevePermitirPrioridadeDeSlaDuplicada() {
        assertThrows(DuplicateKeyException.class,
                () -> slaRepository.save(new SlaConfig(Prioridade.ALTA, 12, "Duplicada")));
    }

    private Solicitacao solicitacao(String protocolo) {
        Solicitacao solicitacao = new Solicitacao();
        solicitacao.setProtocolo(protocolo);
        solicitacao.setDescricao("Descricao");
        return solicitacao;
    }

    private Usuario usuario(String email, String cpf) {
        Usuario usuario = new Usuario();
        usuario.setNome("Duplicado");
        usuario.setEmail(email);
        usuario.setCpf(cpf);
        usuario.setSenhaHash("hash");
        usuario.setPerfil(PerfilUsuario.CIDADAO);
        return usuario;
    }
}
