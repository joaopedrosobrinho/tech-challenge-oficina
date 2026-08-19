package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.cliente.ClienteRequest;
import br.com.fiap.oficina.application.dto.cliente.ClienteResponse;
import br.com.fiap.oficina.application.service.ClienteService;
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
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes da oficina")
@Validated
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @Operation(summary = "Cadastrar cliente", description = "Cadastra um cliente com CPF ou CNPJ válido e e-mail único.")
    public ResponseEntity<ClienteResponse> cadastrar(
            @Valid @RequestBody ClienteRequest request
    ) {
        ClienteResponse response = clienteService.cadastrar(request);

        URI localizacao = URI.create(
                "/api/clientes/" + response.id()
        );

        return ResponseEntity
                .created(localizacao)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar clientes")
    public ResponseEntity<List<ClienteResponse>> listarTodos() {
        return ResponseEntity.ok(
                clienteService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar cliente por ID")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                clienteService.buscarPorId(id)
        );
    }

    @GetMapping("/documento/{cpfCnpj}")
    @Operation(summary = "Buscar cliente por CPF/CNPJ")
    public ResponseEntity<ClienteResponse> buscarPorCpfCnpj(
            @NotBlank @PathVariable String cpfCnpj
    ) {
        return ResponseEntity.ok(
                clienteService.buscarPorCpfCnpj(cpfCnpj)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar cliente")
    public ResponseEntity<ClienteResponse> atualizar(
            @Positive @PathVariable Long id,
            @Valid @RequestBody ClienteRequest request
    ) {
        return ResponseEntity.ok(
                clienteService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir cliente", description = "Exclui somente clientes sem ordens de serviço vinculadas.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@Positive @PathVariable Long id) {
        clienteService.excluir(id);
    }
}
