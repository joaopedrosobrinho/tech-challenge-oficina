package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.ordemservico.AcompanhamentoOrdemServicoResponse;
import br.com.fiap.oficina.application.service.OrdemServicoService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/publico/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Acompanhamento público", description = "Consulta pública e limitada do andamento da ordem de serviço")
@Validated
@SecurityRequirements
public class AcompanhamentoOrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @GetMapping("/{numeroOs}")
    @Operation(summary = "Acompanhar ordem de serviço", description = "Consulta o progresso usando número da OS e placa do veículo, sem expor dados pessoais.")
    public ResponseEntity<AcompanhamentoOrdemServicoResponse> acompanhar(
            @NotBlank @PathVariable String numeroOs,
            @NotBlank @RequestParam String placa
    ) {
        return ResponseEntity.ok(
                ordemServicoService.acompanharPublicamente(numeroOs, placa)
        );
    }
}
