package br.com.fiap.oficina.application.dto.ordemservico;

import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

public record AprovacaoOrcamentoRequest(

        @Size(
                max = 500,
                message = "A observação deve possuir no máximo 500 caracteres"
        )
        @Schema(example = "Orçamento aprovado pelo cliente") String observacao

) {
}
