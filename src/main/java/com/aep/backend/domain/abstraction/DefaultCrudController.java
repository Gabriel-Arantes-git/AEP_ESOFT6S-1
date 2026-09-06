package com.aep.backend.domain.abstraction;

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
    public ResponseEntity<List<E>> listarTodos() {
        return ResponseEntity.ok(getService().listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<E> buscarPorId(@PathVariable String id) {
        return getService().buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<E> criar(@RequestBody @Valid E entity) {
        return ResponseEntity.status(HttpStatus.CREATED).body(getService().salvar(entity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<E> atualizar(@PathVariable String id, @RequestBody @Valid E entity) {
        return ResponseEntity.ok(getService().atualizar(entity));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        getService().deletar(id);
        return ResponseEntity.noContent().build();
    }
}
