package com.aep.backend.domain.abstraction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

public abstract class DefaultCrudController<
        S extends DefaultCrudService<R, E>,
        R extends DefaultCrudRepository<E>,
        E extends DefaultEntity> {

    protected abstract S getService();

    @GetMapping
    @Operation(summary = "Lista todos os registros")
    @ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")
    public ResponseEntity<List<E>> listarTodos() {
        return ResponseEntity.ok(getService().listarTodos());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca um registro pelo id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro encontrado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    public ResponseEntity<E> buscarPorId(@PathVariable String id) {
        return getService().buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Cria um novo registro")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Registro criado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content)
    })
    public ResponseEntity<E> criar(@RequestBody @Valid E entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().salvar(entity));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um registro existente",
            description = "Campos omitidos no corpo mantêm o valor atual do registro.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Registro atualizado"),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado", content = @Content)
    })
    public ResponseEntity<E> atualizar(@PathVariable String id, @RequestBody @Valid E entity) {
        entity.setId(id);
        return ResponseEntity.ok(getService().atualizar(entity));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove um registro",
            description = "Entidades que implementam Ativavel são apenas inativadas (exclusão lógica).")
    @ApiResponse(responseCode = "204", description = "Registro removido ou inativado")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        getService().deletar(id);
        return ResponseEntity.noContent().build();
    }
}
