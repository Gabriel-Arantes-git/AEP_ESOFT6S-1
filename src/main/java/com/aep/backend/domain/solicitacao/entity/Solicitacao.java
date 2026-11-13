package com.aep.backend.domain.solicitacao.entity;

import com.aep.backend.domain.abstraction.DefaultEntity;
import com.aep.backend.domain.categoria.entity.CategoriaResumo;
import com.aep.backend.domain.departamento.entity.DepartamentoResumo;
import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "solicitacao")
@Getter
@Setter
@NoArgsConstructor
public class Solicitacao extends DefaultEntity {

    @Indexed(unique = true)
    private String protocolo;

    private CategoriaResumo categoria;

    private String descricao;

    private Localizacao localizacao;

    private boolean anonimo = false;

    private String usuarioId;

    private String nomeContato;

    private String emailContato;

    private Prioridade prioridade;

    private StatusSolicitacao status = StatusSolicitacao.ABERTO;

    private LocalDateTime prazoAlvo;

    private LocalDateTime dataEncerramento;

    private UsuarioResumo atendente;

    private DepartamentoResumo departamento;

    private List<MovimentacaoResumo> historico = new ArrayList<>();
}
