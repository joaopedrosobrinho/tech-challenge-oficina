package br.com.fiap.oficina.application.dto.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdicionarPecaOrdemRequest(

        @NotNull(message = "O ID da peça é obrigatório")
        @Positive(message = "O ID da peça deve ser maior que zero")
        @Schema(example = "1") Long pecaId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Schema(example = "2") Integer quantidade
) {
}
