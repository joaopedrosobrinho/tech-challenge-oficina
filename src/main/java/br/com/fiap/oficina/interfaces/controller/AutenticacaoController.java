package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.autenticacao.LoginRequest;
import br.com.fiap.oficina.application.dto.autenticacao.LoginResponse;
import br.com.fiap.oficina.application.service.AutenticacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "Autenticação de usuários administrativos")
@SecurityRequirements
public class AutenticacaoController {

    private final AutenticacaoService autenticacaoService;

    @PostMapping("/login")
    @Operation(summary = "Autenticar usuário", description = "Valida as credenciais administrativas e retorna um token JWT do tipo Bearer.")
    public ResponseEntity<LoginResponse> autenticar(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(autenticacaoService.autenticar(request));
    }
}
