package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.servico.ServicoRequest;
import br.com.fiap.oficina.application.dto.servico.ServicoResponse;
import br.com.fiap.oficina.application.service.ServicoService;
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
@RequestMapping("/api/servicos")
@RequiredArgsConstructor
@Tag(name = "Serviços", description = "Catálogo de serviços prestados pela oficina")
@Validated
public class ServicoController {

    private final ServicoService servicoService;

    @PostMapping
    @Operation(summary = "Cadastrar serviço")
    public ResponseEntity<ServicoResponse> cadastrar(
            @Valid @RequestBody ServicoRequest request
    ) {
        ServicoResponse response = servicoService.cadastrar(request);

        URI localizacao = URI.create(
                "/api/servicos/" + response.id()
        );

        return ResponseEntity
                .created(localizacao)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar serviços")
    public ResponseEntity<List<ServicoResponse>> listarTodos() {
        return ResponseEntity.ok(
                servicoService.listarTodos()
        );
    }

    @GetMapping("/ativos")
    @Operation(summary = "Listar serviços ativos")
    public ResponseEntity<List<ServicoResponse>> listarAtivos() {
        return ResponseEntity.ok(
                servicoService.listarAtivos()
        );
    }

    @GetMapping("/pesquisa")
    @Operation(summary = "Pesquisar serviços por nome")
    public ResponseEntity<List<ServicoResponse>> pesquisarPorNome(
            @NotBlank @RequestParam String nome
    ) {
        return ResponseEntity.ok(
                servicoService.pesquisarPorNome(nome)
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar serviço por ID")
    public ResponseEntity<ServicoResponse> buscarPorId(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                servicoService.buscarPorId(id)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar serviço")
    public ResponseEntity<ServicoResponse> atualizar(
            @Positive @PathVariable Long id,
            @Valid @RequestBody ServicoRequest request
    ) {
        return ResponseEntity.ok(
                servicoService.atualizar(id, request)
        );
    }

    @PatchMapping("/{id}/ativar")
    @Operation(summary = "Ativar serviço")
    public ResponseEntity<ServicoResponse> ativar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                servicoService.ativar(id)
        );
    }

    @PatchMapping("/{id}/desativar")
    @Operation(summary = "Desativar serviço")
    public ResponseEntity<ServicoResponse> desativar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                servicoService.desativar(id)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir serviço")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@Positive @PathVariable Long id) {
        servicoService.excluir(id);
    }
}
