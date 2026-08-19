package br.com.fiap.oficina.application.dto.cliente;

import jakarta.validation.constraints.Email;
import br.com.fiap.oficina.application.validation.CpfCnpjValido;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record ClienteRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        @Schema(example = "Maria da Silva") String nome,

        @NotBlank(message = "O CPF ou CNPJ é obrigatório")
        @CpfCnpjValido
        @Size(max = 18, message = "O CPF/CNPJ deve possuir no máximo 18 caracteres")
        @Schema(example = "529.982.247-25") String cpfCnpj,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(
                regexp = "^(?:\\d{10,11}|\\(?\\d{2}\\)?[\\s-]?\\d{4,5}-?\\d{4})$",
                message = "O telefone deve possuir DDD e 10 ou 11 números"
        )
        @Schema(example = "(11) 91234-5678") String telefone,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "O e-mail informado é inválido")
        @Size(max = 150, message = "O e-mail deve possuir no máximo 150 caracteres")
        @Schema(example = "maria.silva@email.com") String email
) {
}
