package com.aep.backend.domain.abstraction;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import java.time.LocalDateTime;

@Getter
@Setter
public abstract class DefaultEntity {

    @Id
    private String id;

    @CreatedDate
    private LocalDateTime dataCadastro;
}
