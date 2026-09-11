package com.aep.backend.auth;

import com.aep.backend.auth.dto.AuthResponse;
import com.aep.backend.auth.dto.LoginRequest;
import com.aep.backend.domain.usuario.entity.Usuario;
import com.aep.backend.infra.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Emissão de token JWT")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica um usuário e devolve o token JWT",
            description = "O token retornado deve ser enviado nas demais requisições no header "
                    + "Authorization, no formato: Bearer <token>.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso"),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos", content = @Content)
    })
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));
        String token = jwtTokenProvider.generateToken(request.email());
        Usuario usuario = (Usuario) auth.getPrincipal();
        return ResponseEntity.ok(new AuthResponse(token, usuario.getEmail(), usuario.getNome(), usuario.getPerfil().name()));
    }
}
