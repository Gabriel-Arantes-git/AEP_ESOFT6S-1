package com.aep.backend.domain.solicitacao.controller;

import com.aep.backend.domain.enums.StatusSolicitacao;
import com.aep.backend.domain.solicitacao.dto.*;
import com.aep.backend.domain.solicitacao.service.SolicitacaoService;
import com.aep.backend.domain.usuario.entity.Usuario;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/solicitacoes")
@RequiredArgsConstructor
@Tag(name = "Solicitações", description = "Abertura, acompanhamento e tramitação de denúncias")
public class SolicitacaoController {

    private final SolicitacaoService solicitacaoService;

    @PostMapping
    @Operation(summary = "Abre uma denúncia identificada",
            description = "A denúncia fica vinculada ao usuário autenticado e recebe um protocolo "
                    + "no formato DEN-<ano>-<sequencial>. O status inicial é ABERTO.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Denúncia aberta"),
            @ApiResponse(responseCode = "400", description = "Descrição vazia ou categoria inexistente", content = @Content)
    })
    public ResponseEntity<SolicitacaoResponse> criar(
            @RequestBody @Valid SolicitacaoRequest req,
            @AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitacaoResponse.from(solicitacaoService.criar(req, usuario)));
    }

    @PostMapping("/anonima")
    @SecurityRequirements
    @Operation(summary = "Abre uma denúncia anônima",
            description = "Endpoint público: a denúncia não é vinculada a nenhum usuário. "
                    + "A descrição precisa ter no mínimo 50 caracteres. "
                    + "O campo anonimo do corpo é ignorado e sempre tratado como true.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Denúncia anônima aberta"),
            @ApiResponse(responseCode = "400", description = "Descrição com menos de 50 caracteres ou categoria inexistente", content = @Content)
    })
    public ResponseEntity<SolicitacaoResponse> criarAnonima(@RequestBody @Valid SolicitacaoRequest req) {
        SolicitacaoRequest reqAnonima = new SolicitacaoRequest(
                req.categoriaId(), req.descricao(), req.bairro(),
                req.logradouro(), req.referencia(), true,
                req.nomeContato(), req.emailContato(),
                req.latitude(), req.longitude(), req.cep()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SolicitacaoResponse.from(solicitacaoService.criar(reqAnonima, null)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GESTOR')")
    @Operation(summary = "Lista as denúncias, opcionalmente filtrando por status",
            description = "Requer perfil ATENDENTE ou GESTOR. Sem filtro, retorna da mais recente para a mais antiga.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Denúncias retornadas"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content)
    })
    public ResponseEntity<List<SolicitacaoResponse>> listar(
            @Parameter(description = "Filtra por status do fluxo de atendimento")
            @RequestParam(required = false) StatusSolicitacao status) {
        List<SolicitacaoResponse> lista = status != null
                ? solicitacaoService.listarPorStatus(status).stream().map(SolicitacaoResponse::from).toList()
                : solicitacaoService.listarTodas().stream().map(SolicitacaoResponse::from).toList();
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/minhas")
    @Operation(summary = "Lista as denúncias abertas pelo usuário autenticado",
            description = "Denúncias anônimas não aparecem aqui, pois não guardam vínculo com o autor.")
    @ApiResponse(responseCode = "200", description = "Denúncias retornadas")
    public ResponseEntity<List<SolicitacaoResponse>> minhas(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(
                solicitacaoService.listarMinhas(usuario.getId()).stream().map(SolicitacaoResponse::from).toList());
    }

    @GetMapping("/publicas")
    @SecurityRequirements
    @Operation(summary = "Lista as denúncias para consulta pública")
    @ApiResponse(responseCode = "200", description = "Denúncias retornadas")
    public ResponseEntity<List<SolicitacaoResponse>> publicas() {
        return ResponseEntity.ok(
                solicitacaoService.listarTodas().stream().map(SolicitacaoResponse::from).toList());
    }

    @GetMapping("/anonimas")
    @SecurityRequirements
    @Operation(summary = "Lista somente as denúncias anônimas")
    @ApiResponse(responseCode = "200", description = "Denúncias retornadas")
    public ResponseEntity<List<SolicitacaoResponse>> anonimas() {
        return ResponseEntity.ok(
                solicitacaoService.listarAnonimas().stream().map(SolicitacaoResponse::from).toList());
    }

    @GetMapping("/protocolo/{protocolo}")
    @Operation(summary = "Busca uma denúncia pelo número de protocolo")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Denúncia encontrada"),
            @ApiResponse(responseCode = "400", description = "Protocolo inexistente", content = @Content)
    })
    public ResponseEntity<SolicitacaoResponse> buscarPorProtocolo(
            @Parameter(description = "Protocolo no formato DEN-<ano>-<sequencial>", example = "DEN-2026-00001")
            @PathVariable String protocolo) {
        return ResponseEntity.ok(SolicitacaoResponse.from(solicitacaoService.buscarPorProtocolo(protocolo)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ATENDENTE', 'GESTOR')")
    @Operation(summary = "Move a denúncia para o próximo status",
            description = "Requer perfil ATENDENTE ou GESTOR. Transições válidas: ABERTO → TRIAGEM, "
                    + "TRIAGEM → EM_EXECUCAO ou ENCERRADO, EM_EXECUCAO → RESOLVIDO, RESOLVIDO → ENCERRADO. "
                    + "Na TRIAGEM os campos prioridade e departamentoId são obrigatórios, e o prazo alvo "
                    + "é calculado a partir do SLA da prioridade. Somente o GESTOR pode ENCERRAR.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Status atualizado"),
            @ApiResponse(responseCode = "400", description = "Denúncia inexistente ou dados de triagem faltando", content = @Content),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content),
            @ApiResponse(responseCode = "409", description = "Transição de status inválida ou encerramento por perfil não gestor", content = @Content)
    })
    public ResponseEntity<Void> moverStatus(
            @PathVariable String id,
            @RequestBody @Valid MoverStatusRequest req,
            @AuthenticationPrincipal Usuario responsavel) {
        solicitacaoService.moverStatus(id, req, responsavel);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/movimentacoes")
    @Operation(summary = "Lista o histórico de movimentações da denúncia",
            description = "Cada troca de status gera uma movimentação com responsável e comentário.")
    @ApiResponse(responseCode = "200", description = "Histórico retornado")
    public ResponseEntity<List<MovimentacaoResponse>> historico(@PathVariable String id) {
        return ResponseEntity.ok(
                solicitacaoService.buscarHistorico(id).stream().map(MovimentacaoResponse::from).toList());
    }

    @GetMapping("/{id}/logs")
    @PreAuthorize("hasRole('GESTOR')")
    @Operation(summary = "Lista a trilha de auditoria da denúncia",
            description = "Requer perfil GESTOR.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Logs retornados"),
            @ApiResponse(responseCode = "403", description = "Perfil sem permissão", content = @Content)
    })
    public ResponseEntity<List<LogAcaoResponse>> logs(@PathVariable String id) {
        return ResponseEntity.ok(
                solicitacaoService.buscarLogs(id).stream().map(LogAcaoResponse::from).toList());
    }
}
