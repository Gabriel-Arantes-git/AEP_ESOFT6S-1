package com.aep.backend.domain.solicitacao.entity;

import com.aep.backend.domain.abstraction.DefaultEntity;
import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "movimentacao")
@Getter
@Setter
@NoArgsConstructor
public class Movimentacao extends DefaultEntity {

    private String solicitacaoId;

    private StatusSolicitacao statusAnterior;

    private StatusSolicitacao statusNovo;

    private String comentario;

    private UsuarioResumo responsavel;

    private String justificativaAtraso;
}
