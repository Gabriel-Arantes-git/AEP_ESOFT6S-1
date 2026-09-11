package com.aep.backend.domain.solicitacao.service;

import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.departamento.entity.DepartamentoResumo;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.enums.PerfilUsuario;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.log.entity.LogAcao;
import com.aep.backend.domain.log.repository.LogRepository;
import com.aep.backend.domain.sla.service.SlaService;
import com.aep.backend.domain.solicitacao.dto.MoverStatusRequest;
import com.aep.backend.domain.solicitacao.dto.SolicitacaoRequest;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import com.aep.backend.domain.solicitacao.repository.MovimentacaoRepository;
import com.aep.backend.domain.solicitacao.repository.SolicitacaoRepository;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolicitacaoService {

    private final SolicitacaoRepository solicitacaoRepository;
    private final MovimentacaoRepository movimentacaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DepartamentoRepository departamentoRepository;
    private final SlaService slaService;
    private final LogRepository logRepository;

    @Transactional
    public Solicitacao criar(SolicitacaoRequest req, Usuario solicitante) {
        validarDescricao(req.descricao(), req.anonimo());

        Solicitacao s = new Solicitacao();
        s.setProtocolo(gerarProtocolo());
        s.setCategoria(CategoriaResumo.from(categoriaRepository.findById(req.categoriaId())
                .orElseThrow(() -> new IllegalArgumentException("Categoria não encontrada: " + req.categoriaId()))));
        s.setDescricao(req.descricao());
        s.setBairro(req.bairro());
        s.setLogradouro(req.logradouro());
        s.setReferencia(req.referencia());
        s.setLatitude(req.latitude());
        s.setLongitude(req.longitude());
        s.setCep(req.cep());
        s.setAnonimo(req.anonimo());
        s.setNomeContato(req.nomeContato());
        s.setEmailContato(req.emailContato());
        if (!req.anonimo()) s.setUsuarioId(solicitante.getId());
        s.setStatus(StatusSolicitacao.ABERTO);
        solicitacaoRepository.save(s);

        registrarMovimentacao(s, null, StatusSolicitacao.ABERTO, "Solicitação aberta.", solicitante);
        logRepository.save(new LogAcao(solicitante, "ABRIR_SOLICITACAO", "solicitacao", s.getId(), s.getProtocolo()));
        log.info("Solicitação criada: protocolo={} usuario={}", s.getProtocolo(),
                solicitante != null ? solicitante.getId() : "anônimo");
        return s;
    }

    @Transactional
    public void moverStatus(String id, MoverStatusRequest req, Usuario responsavel) {
        Solicitacao s = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Solicitação não encontrada: " + id));

        if (!s.getStatus().podeMoverPara(req.novoStatus()))
            throw new IllegalStateException(
                    "Transição inválida: " + s.getStatus() + " → " + req.novoStatus());

        if (req.novoStatus() == StatusSolicitacao.ENCERRADO && responsavel.getPerfil() != PerfilUsuario.GESTOR)
            throw new IllegalStateException("Apenas o gestor pode encerrar a solicitação");

        StatusSolicitacao anterior = s.getStatus();

        if (req.novoStatus() == StatusSolicitacao.TRIAGEM) {
            if (req.prioridade() == null) throw new IllegalArgumentException("Prioridade obrigatória na triagem");
            if (req.departamentoId() == null) throw new IllegalArgumentException("Departamento obrigatório na triagem");
            s.setPrioridade(req.prioridade());
            s.setDepartamento(DepartamentoResumo.from(departamentoRepository.findById(req.departamentoId())
                    .orElseThrow(() -> new IllegalArgumentException("Departamento não encontrado: " + req.departamentoId()))));
            s.setPrazoAlvo(calcularPrazo(req.prioridade()));
            s.setAtendente(UsuarioResumo.from(responsavel));
        }

        if (req.novoStatus() == StatusSolicitacao.RESOLVIDO || req.novoStatus() == StatusSolicitacao.ENCERRADO)
            s.setDataEncerramento(LocalDateTime.now());

        s.setStatus(req.novoStatus());
        solicitacaoRepository.save(s);

        registrarMovimentacao(s, anterior, req.novoStatus(), req.comentario(), responsavel);
        logRepository.save(new LogAcao(responsavel, "MOVER_STATUS", "solicitacao", s.getId(),
                anterior + " → " + req.novoStatus()));
        log.info("Status movido: protocolo={} {} → {} responsavel={}",
                s.getProtocolo(), anterior, req.novoStatus(), responsavel.getId());
    }

    public Solicitacao buscarPorProtocolo(String protocolo) {
        return solicitacaoRepository.findByProtocolo(protocolo)
                .orElseThrow(() -> new IllegalArgumentException("Protocolo não encontrado: " + protocolo));
    }

    public List<Solicitacao> listarTodas() {
        return solicitacaoRepository.findAllByOrderByDataCadastroDesc();
    }

    public List<Solicitacao> listarPorStatus(StatusSolicitacao status) {
        return solicitacaoRepository.findAllByStatusOrderByDataCadastroAsc(status);
    }

    public List<Solicitacao> listarMinhas(String usuarioId) {
        return solicitacaoRepository.findAllByUsuarioIdOrderByDataCadastroDesc(usuarioId);
    }

    public List<Solicitacao> listarAnonimas() {
        return solicitacaoRepository.findAllByUsuarioIdIsNullOrderByDataCadastroDesc();
    }

    public List<Movimentacao> buscarHistorico(String solicitacaoId) {
        return movimentacaoRepository.findAllBySolicitacaoIdOrderByDataCadastroAsc(solicitacaoId);
    }

    public List<LogAcao> buscarLogs(String solicitacaoId) {
        return logRepository.findAllByEntidadeAndEntidadeIdOrderByDataCadastroAsc("solicitacao", solicitacaoId);
    }

    private void validarDescricao(String descricao, boolean anonimo) {
        if (descricao == null || descricao.isBlank())
            throw new IllegalArgumentException("Descrição obrigatória");
        if (anonimo && descricao.length() < 50)
            throw new IllegalArgumentException("Descrição deve ter no mínimo 50 caracteres para denúncia anônima");
    }

    private String gerarProtocolo() {
        int ano = Year.now().getValue();
        String prefixo = "DEN-" + ano + "-";
        long count = solicitacaoRepository.countByProtocoloStartingWith(prefixo);
        return prefixo + String.format("%05d", count + 1);
    }

    private LocalDateTime calcularPrazo(Prioridade prioridade) {
        return LocalDateTime.now().plusHours(slaService.buscarPorPrioridade(prioridade).getPrazoHoras());
    }

    private void registrarMovimentacao(Solicitacao s, StatusSolicitacao anterior,
                                       StatusSolicitacao novo, String comentario, Usuario responsavel) {
        Movimentacao m = new Movimentacao();
        m.setSolicitacaoId(s.getId());
        m.setStatusAnterior(anterior);
        m.setStatusNovo(novo);
        m.setComentario(comentario);
        m.setResponsavel(UsuarioResumo.from(responsavel));
        movimentacaoRepository.save(m);
    }
}
