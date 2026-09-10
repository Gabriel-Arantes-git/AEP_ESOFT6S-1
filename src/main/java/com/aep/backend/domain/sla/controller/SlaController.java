package com.aep.backend.domain.sla.controller;

import com.aep.backend.domain.sla.entity.SlaConfig;
import com.aep.backend.domain.sla.service.SlaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sla")
@RequiredArgsConstructor
@Tag(name = "SLA", description = "Prazos de atendimento por prioridade")
public class SlaController {

    private final SlaService slaService;

    @GetMapping
    @SecurityRequirements
    @Operation(summary = "Lista os prazos de SLA configurados",
            description = "O prazo alvo de uma solicitação é calculado na triagem, "
                    + "somando as horas da prioridade escolhida à data da triagem.")
    @ApiResponse(responseCode = "200", description = "Configurações retornadas")
    public ResponseEntity<List<SlaConfig>> listar() {
        return ResponseEntity.ok(slaService.listarTodos());
    }
}
