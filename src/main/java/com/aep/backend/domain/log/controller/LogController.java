package com.aep.backend.domain.log.controller;

import com.aep.backend.domain.log.service.LogService;
import com.aep.backend.domain.solicitacao.dto.LogAcaoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Tag(name = "Logs", description = "Trilha de auditoria das ações do sistema")
public class LogController {

    private final LogService logService;

    @GetMapping
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Lista todas as ações registradas",
            description = "Requer perfil GESTOR. Ordenado da ação mais recente para a mais antiga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs retornados"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content)
    })
    public ResponseEntity<List<LogAcaoResponse>> listar() {
        return ResponseEntity.ok(logService.listarTodos().stream().map(LogAcaoResponse::from).toList());
    }
}
