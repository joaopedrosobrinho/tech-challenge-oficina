package br.com.fiap.oficina.application.dto.ordemservico;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record OrdemServicoRequest(

        @NotNull(message = "O cliente é obrigatório")
        @Positive(message = "O ID do cliente deve ser maior que zero")
        @Schema(example = "1") Long clienteId,

        @NotNull(message = "O veículo é obrigatório")
        @Positive(message = "O ID do veículo deve ser maior que zero")
        @Schema(example = "1") Long veiculoId,

        @Size(
                max = 1000,
                message = "As observações devem possuir no máximo 1000 caracteres"
        )
        @Schema(example = "Cliente relata ruído ao frear") String observacoes,

        @Valid
        @NotEmpty(
                message = "A ordem de serviço deve possuir pelo menos um serviço"
        )
        List<ItemServicoRequest> servicos,

        @Valid
        List<ItemPecaRequest> pecas
) {
}
