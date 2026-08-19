package br.com.fiap.oficina.application.dto.peca;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record PecaRequest(

        @NotBlank(message = "O código da peça é obrigatório")
        @Size(max = 50, message = "O código deve possuir no máximo 50 caracteres")
        @Schema(example = "FLT-001") String codigo,

        @NotBlank(message = "O nome da peça é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        @Schema(example = "Filtro de óleo") String nome,

        @Size(max = 500, message = "A descrição deve possuir no máximo 500 caracteres")
        @Schema(example = "Filtro compatível com o veículo") String descricao,

        @NotNull(message = "O valor é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor deve ser maior que zero"
        )
        @Digits(
                integer = 10,
                fraction = 2,
                message = "O valor deve possuir no máximo 10 dígitos inteiros e 2 decimais"
        )
        @Schema(example = "45.90") BigDecimal valor,

        @NotNull(message = "A quantidade em estoque é obrigatória")
        @Min(
                value = 0,
                message = "A quantidade em estoque não pode ser negativa"
        )
        @Schema(example = "20") Integer quantidadeEstoque,

        @Schema(example = "true") Boolean ativo
) {
}
