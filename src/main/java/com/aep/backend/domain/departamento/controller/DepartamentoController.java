package com.aep.backend.domain.departamento.controller;

import com.aep.backend.domain.abstraction.DefaultCrudController;
import com.aep.backend.domain.departamento.entity.DepartamentoDestino;
import com.aep.backend.domain.departamento.repository.DepartamentoRepository;
import com.aep.backend.domain.departamento.service.DepartamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/departamentos")
@RequiredArgsConstructor
@Tag(name = "Departamentos", description = "Órgãos de destino para encaminhamento das denúncias")
public class DepartamentoController extends DefaultCrudController<DepartamentoService, DepartamentoRepository, DepartamentoDestino> {

    private final DepartamentoService departamentoService;

    @Override
    protected DepartamentoService getService() {
        return departamentoService;
    }

    @GetMapping("/ativos")
    @SecurityRequirements
    @Operation(summary = "Lista apenas os departamentos ativos",
            description = "Endpoint público, usado na triagem para escolher o órgão responsável.")
    @ApiResponse(responseCode = "200", description = "Departamentos ativos retornados")
    public ResponseEntity<List<DepartamentoDestino>> listarAtivos() {
        return ResponseEntity.ok(departamentoService.listarAtivos());
    }
}
