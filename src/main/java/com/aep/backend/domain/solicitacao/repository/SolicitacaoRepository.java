package com.aep.backend.domain.solicitacao.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.entity.Solicitacao;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitacaoRepository extends DefaultCrudRepository<Solicitacao> {
    Optional<Solicitacao> findByProtocolo(String protocolo);
    List<Solicitacao> findAllByStatusOrderByDataCadastroAsc(StatusSolicitacao status);
    List<Solicitacao> findAllByOrderByDataCadastroDesc();
    List<Solicitacao> findAllByUsuarioIdOrderByDataCadastroDesc(String usuarioId);
    List<Solicitacao> findAllByUsuarioIdIsNullOrderByDataCadastroDesc();
    long countByProtocoloStartingWith(String prefixo);
}
