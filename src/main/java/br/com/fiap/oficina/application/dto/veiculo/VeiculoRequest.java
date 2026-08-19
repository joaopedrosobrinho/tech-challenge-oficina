package br.com.fiap.oficina.application.dto.veiculo;

import br.com.fiap.oficina.application.validation.PlacaValida;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record VeiculoRequest(

        @NotBlank(message = "A placa é obrigatória")
        @PlacaValida
        @Schema(example = "ABC1D23") String placa,

        @NotBlank(message = "A marca é obrigatória")
        @Size(max = 100, message = "A marca deve possuir no máximo 100 caracteres")
        @Schema(example = "Honda") String marca,

        @NotBlank(message = "O modelo é obrigatório")
        @Size(max = 100, message = "O modelo deve possuir no máximo 100 caracteres")
        @Schema(example = "Civic") String modelo,

        @NotNull(message = "O ano é obrigatório")
        @Min(value = 1900, message = "O ano deve ser igual ou posterior a 1900")
        @Max(value = 2100, message = "O ano deve ser igual ou anterior a 2100")
        @Schema(example = "2022") Integer ano,

        @NotNull(message = "O cliente é obrigatório")
        @Positive(message = "O ID do cliente deve ser maior que zero")
        @Schema(example = "1") Long clienteId
) {
}
