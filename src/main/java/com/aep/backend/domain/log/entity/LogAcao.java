package com.aep.backend.domain.log.entity;

import com.aep.backend.domain.abstraction.DefaultEntity;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.entity.UsuarioResumo;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "log_acao")
@Getter
@Setter
@NoArgsConstructor
public class LogAcao extends DefaultEntity {

    private UsuarioResumo usuario;

    private String acao;

    private String entidade;

    private String entidadeId;

    private String detalhes;

    public LogAcao(Usuario usuario, String acao, String entidade, String entidadeId, String detalhes) {
        this.usuario = UsuarioResumo.from(usuario);
        this.acao = acao;
        this.entidade = entidade;
        this.entidadeId = entidadeId;
        this.detalhes = detalhes;
    }
}
