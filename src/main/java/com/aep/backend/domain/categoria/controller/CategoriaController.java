package com.aep.backend.domain.categoria.controller;

import com.aep.backend.domain.abstraction.DefaultCrudController;
import com.aep.backend.domain.categoria.entity.Categoria;
import com.aep.backend.domain.categoria.repository.CategoriaRepository;
import com.aep.backend.domain.categoria.service.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias", description = "Categorias de denúncia")
public class CategoriaController extends DefaultCrudController<CategoriaService, CategoriaRepository, Categoria> {

    private final CategoriaService categoriaService;

    @Override
    protected CategoriaService getService() {
        return categoriaService;
    }

    @GetMapping("/ativas")
    @SecurityRequirements
    @Operation(summary = "Lista apenas as categorias ativas",
            description = "Endpoint público, usado para montar o formulário de abertura de denúncia.")
    @ApiResponse(responseCode = "200", description = "Categorias ativas retornadas")
    public ResponseEntity<List<Categoria>> listarAtivas() {
        return ResponseEntity.ok(categoriaService.listarAtivas());
    }
}
