package com.aep.backend.domain.categoria.entity;

import com.aep.backend.domain.abstraction.Ativavel;
import com.aep.backend.domain.abstraction.DefaultEntity;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "categoria")
@Getter
@Setter
@NoArgsConstructor
public class Categoria extends DefaultEntity implements Ativavel {

    @Indexed(unique = true)
    private String nome;

    private String descricao;

    private boolean ativo = true;

    public Categoria(String nome, String descricao) {
        this.nome = nome;
        this.descricao = descricao;
    }
}
