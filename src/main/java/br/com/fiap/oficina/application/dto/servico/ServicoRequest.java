package br.com.fiap.oficina.application.dto.servico;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public record ServicoRequest(

        @NotBlank(message = "O nome do serviço é obrigatório")
        @Size(max = 150, message = "O nome deve possuir no máximo 150 caracteres")
        @Schema(example = "Troca de óleo") String nome,

        @Size(max = 500, message = "A descrição deve possuir no máximo 500 caracteres")
        @Schema(example = "Substituição do óleo e do filtro") String descricao,

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
        @Schema(example = "150.00") BigDecimal valor,

        @NotNull(message = "O tempo estimado é obrigatório")
        @Min(
                value = 1,
                message = "O tempo estimado deve ser maior que zero"
        )
        @Schema(example = "60") Integer tempoEstimadoMinutos,

        @Schema(example = "true") Boolean ativo
) {
}
