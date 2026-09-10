package com.aep.backend.domain.usuario.controller;

import com.aep.backend.domain.abstraction.DefaultCrudController;
import com.aep.backend.domain.usuario.dto.UsuarioRequest;
import com.aep.backend.domain.usuario.dto.UsuarioResponse;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.domain.usuario.repository.UsuarioRepository;
import com.aep.backend.domain.usuario.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "Cadastro e manutenção de usuários")
public class UsuarioController extends DefaultCrudController<UsuarioService, UsuarioRepository, Usuario> {

    private final UsuarioService usuarioService;

    @Override
    protected UsuarioService getService() {
        return usuarioService;
    }

    @PostMapping("/cadastrar")
    @SecurityRequirements
    @Operation(summary = "Cadastra um novo usuário",
            description = "Endpoint público de autocadastro. A senha é gravada com hash BCrypt "
                    + "e o e-mail precisa ser único.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário cadastrado"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos ou e-mail já cadastrado", content = @Content)
    })
    public ResponseEntity<UsuarioResponse> cadastrar(@RequestBody @Valid UsuarioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(UsuarioResponse.from(usuarioService.cadastrar(request)));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Cria um usuário a partir da entidade bruta (uso administrativo)",
            description = "Requer perfil GESTOR. Não aplica hash na senha nem valida e-mail duplicado — "
                    + "para criar usuários use POST /usuarios/cadastrar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content)
    })
    public ResponseEntity<Usuario> criar(@RequestBody @Valid Usuario entity) {
        return super.criar(entity);
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Inativa um usuário",
            description = "Requer perfil GESTOR. Exclusão lógica: o usuário é marcado como inativo "
                    + "e deixa de conseguir autenticar.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário inativado"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable String id) {
        return super.deletar(id);
    }
}
