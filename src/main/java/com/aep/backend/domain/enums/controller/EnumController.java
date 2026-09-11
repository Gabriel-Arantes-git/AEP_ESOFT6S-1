package com.aep.backend.domain.enums.controller;

import com.aep.backend.domain.enums.Prioridade;
import com.aep.backend.domain.enums.StatusSolicitacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/enums")
@SecurityRequirements
@Tag(name = "Enums", description = "Valores de domínio para preenchimento de formulários")
public class EnumController {

    @GetMapping("/status-solicitacao")
    @Operation(summary = "Lista os status possíveis de uma solicitação",
            description = "Fluxo permitido: ABERTO → TRIAGEM → EM_EXECUCAO → RESOLVIDO → ENCERRADO. "
                    + "TRIAGEM também pode ir direto para ENCERRADO.")
    public ResponseEntity<List<String>> statusSolicitacao() {
        return ResponseEntity.ok(Arrays.stream(StatusSolicitacao.values()).map(Enum::name).toList());
    }

    @GetMapping("/prioridades")
    @Operation(summary = "Lista as prioridades disponíveis",
            description = "Cada prioridade tem um prazo de SLA associado, consultável em GET /sla.")
    public ResponseEntity<List<String>> prioridades() {
        return ResponseEntity.ok(Arrays.stream(Prioridade.values()).map(Enum::name).toList());
    }
}
