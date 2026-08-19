package br.com.fiap.oficina.application.dto.ordemservico;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

public record AdicionarServicoOrdemRequest(

        @NotNull(message = "O ID do serviço é obrigatório")
        @Positive(message = "O ID do serviço deve ser maior que zero")
        @Schema(example = "1") Long servicoId,

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        @Schema(example = "1") Integer quantidade
) {
}
