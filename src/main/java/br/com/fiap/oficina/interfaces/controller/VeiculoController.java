package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.veiculo.VeiculoRequest;
import br.com.fiap.oficina.application.dto.veiculo.VeiculoResponse;
import br.com.fiap.oficina.application.service.VeiculoService;
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
@RequestMapping("/api/veiculos")
@RequiredArgsConstructor
@Tag(name = "Veículos", description = "Cadastro e consulta de veículos dos clientes")
@Validated
public class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    @Operation(summary = "Cadastrar veículo")
    public ResponseEntity<VeiculoResponse> cadastrar(
            @Valid @RequestBody VeiculoRequest request
    ) {
        VeiculoResponse response = veiculoService.cadastrar(request);

        URI localizacao = URI.create(
                "/api/veiculos/" + response.id()
        );

        return ResponseEntity
                .created(localizacao)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar veículos")
    public ResponseEntity<List<VeiculoResponse>> listarTodos() {
        return ResponseEntity.ok(
                veiculoService.listarTodos()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar veículo por ID")
    public ResponseEntity<VeiculoResponse> buscarPorId(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                veiculoService.buscarPorId(id)
        );
    }

    @GetMapping("/placa/{placa}")
    @Operation(summary = "Buscar veículo por placa")
    public ResponseEntity<VeiculoResponse> buscarPorPlaca(
            @NotBlank @PathVariable String placa
    ) {
        return ResponseEntity.ok(
                veiculoService.buscarPorPlaca(placa)
        );
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar veículos de um cliente")
    public ResponseEntity<List<VeiculoResponse>> listarPorCliente(
            @Positive @PathVariable Long clienteId
    ) {
        return ResponseEntity.ok(
                veiculoService.listarPorCliente(clienteId)
        );
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo")
    public ResponseEntity<VeiculoResponse> atualizar(
            @Positive @PathVariable Long id,
            @Valid @RequestBody VeiculoRequest request
    ) {
        return ResponseEntity.ok(
                veiculoService.atualizar(id, request)
        );
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir veículo", description = "Exclui somente veículos sem ordens de serviço vinculadas.")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@Positive @PathVariable Long id) {
        veiculoService.excluir(id);
    }
}
