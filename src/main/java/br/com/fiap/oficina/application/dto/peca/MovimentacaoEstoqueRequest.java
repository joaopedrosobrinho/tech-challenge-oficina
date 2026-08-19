package br.com.fiap.oficina.application.dto.peca;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

public record MovimentacaoEstoqueRequest(

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Schema(example = "5") Integer quantidade
) {
}
