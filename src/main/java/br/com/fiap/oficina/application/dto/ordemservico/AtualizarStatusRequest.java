package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusRequest(

        @NotNull(message = "O status é obrigatório")
        StatusOrdemServico status
) {
}