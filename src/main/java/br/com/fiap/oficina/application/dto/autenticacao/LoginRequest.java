package br.com.fiap.oficina.application.dto.autenticacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(max = 150, message = "O e-mail deve possuir no máximo 150 caracteres")
        @Schema(example = "admin@oficina.com")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(max = 100, message = "A senha deve possuir no máximo 100 caracteres")
        @Schema(example = "admin123", format = "password")
        String senha
) {
}
