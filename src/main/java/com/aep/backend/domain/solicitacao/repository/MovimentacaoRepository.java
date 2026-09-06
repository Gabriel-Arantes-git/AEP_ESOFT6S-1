package com.aep.backend.domain.solicitacao.repository;

import com.aep.backend.domain.abstraction.DefaultCrudRepository;
import com.aep.backend.domain.solicitacao.entity.Movimentacao;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimentacaoRepository extends DefaultCrudRepository<Movimentacao> {
    List<Movimentacao> findAllBySolicitacaoIdOrderByDataCadastroAsc(String solicitacaoId);
}
