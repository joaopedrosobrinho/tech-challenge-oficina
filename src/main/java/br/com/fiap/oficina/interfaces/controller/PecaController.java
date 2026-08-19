package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.peca.MovimentacaoEstoqueRequest;
import br.com.fiap.oficina.application.dto.peca.PecaRequest;
import br.com.fiap.oficina.application.dto.peca.PecaResponse;
import br.com.fiap.oficina.application.service.PecaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/pecas")
@RequiredArgsConstructor
@Tag(name = "Peças e estoque", description = "Catálogo de peças e movimentações de estoque")
@Validated
public class PecaController {

    private final PecaService pecaService;

    @PostMapping
    @Operation(summary = "Cadastrar peça")
    public ResponseEntity<PecaResponse> cadastrar(
            @Valid @RequestBody PecaRequest request
    ) {
        PecaResponse response = pecaService.cadastrar(request);

        URI localizacao = URI.create(
                "/api/pecas/" + response.id()
        );

        return ResponseEntity
                .created(localizacao)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar peças")
    public ResponseEntity<List<PecaResponse>> listarTodas() {
        return ResponseEntity.ok(
                pecaService.listarTodas()
        );
    }

    @GetMapping("/ativas")
    @Operation(summary = "Listar peças ativas")
    public ResponseEntity<List<PecaResponse>> listarAtivas() {
        return ResponseEntity.ok(
                pecaService.listarAtivas()
        );
    }

    @GetMapping("/pesquisa")
    @Operation(summary = "Pesquisar peças por nome")
    public ResponseEntity<List<PecaResponse>> pesquisarPorNome(
            @NotBlank @RequestParam String nome
    ) {
        return ResponseEntity.ok(
                pecaService.pesquisarPorNome(nome)
        );
    }

    @GetMapping("/estoque-baixo")
    @Operation(summary = "Listar peças com estoque baixo")
    public ResponseEntity<List<PecaResponse>> listarEstoqueBaixo(
            @Positive @RequestParam(defaultValue = "5") Integer limite
    ) {
        return ResponseEntity.ok(
                pecaService.listarEstoqueBaixo(limite)
        );
    }

    @GetMapping("/codigo/{codigo}")
    @Operation(summary = "Buscar peça por código")
    public ResponseEntity<PecaResponse> buscarPorCodigo(
            @NotBlank @PathVariable String codigo
    ) {
        return ResponseEntity.ok(
                pecaService.buscarPorCodigo(codigo)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar peça por ID")
    public ResponseEntity<PecaResponse> buscarPorId(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pecaService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar peça")
    public ResponseEntity<PecaResponse> atualizar(
            @Positive @PathVariable Long id,
            @Valid @RequestBody PecaRequest request
    ) {
        return ResponseEntity.ok(
                pecaService.atualizar(id, request)
        );
    }

    @PatchMapping("/{id}/entrada-estoque")
    @Operation(summary = "Registrar entrada no estoque")
    public ResponseEntity<PecaResponse> adicionarEstoque(
            @Positive @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request
    ) {
        return ResponseEntity.ok(
                pecaService.adicionarEstoque(id, request)
        );
    }

    @PatchMapping("/{id}/saida-estoque")
    @Operation(summary = "Registrar saída do estoque", description = "Impede que o estoque fique negativo.")
    public ResponseEntity<PecaResponse> retirarEstoque(
            @Positive @PathVariable Long id,
            @Valid @RequestBody MovimentacaoEstoqueRequest request
    ) {
        return ResponseEntity.ok(
                pecaService.retirarEstoque(id, request)
        );
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar peça")
    public ResponseEntity<PecaResponse> ativar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pecaService.ativar(id)
        );
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar peça")
    public ResponseEntity<PecaResponse> desativar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                pecaService.desativar(id)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir peça")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@Positive @PathVariable Long id) {
        pecaService.excluir(id);
    }
}
