package br.com.fiap.oficina.interfaces.controller;
import br.com.fiap.oficina.application.dto.ordemservico.DecisaoExternaRequest;
import br.com.fiap.oficina.application.service.AprovacaoExternaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
@RestController
@RequestMapping("/api/integracoes/ordens-servico")
@RequiredArgsConstructor
@SecurityRequirement(name = "integracaoKey")
public class AprovacaoExternaController {
    private final AprovacaoExternaService service;
    @PostMapping("/{id}/aprovacao")
    public ResponseEntity<Void> receber(@PathVariable Long id, @Valid @RequestBody DecisaoExternaRequest request) {
        service.receber(id, request);
        return ResponseEntity.noContent().build();
    }
}
