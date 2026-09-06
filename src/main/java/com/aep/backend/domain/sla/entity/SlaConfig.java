package com.aep.backend.domain.sla.entity;

import com.aep.backend.domain.abstraction.DefaultEntity;
import com.aep.backend.domain.enums.Prioridade;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "sla_config")
@Getter
@Setter
@NoArgsConstructor
public class SlaConfig extends DefaultEntity {

    @Indexed(unique = true)
    private Prioridade prioridade;

    private int prazoHoras;

    private String descricao;

    public SlaConfig(Prioridade prioridade, int prazoHoras, String descricao) {
        this.prioridade = prioridade;
        this.prazoHoras = prazoHoras;
        this.descricao = descricao;
    }
}
