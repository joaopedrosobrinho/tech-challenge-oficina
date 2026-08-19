package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record DuracaoExecucaoResponse(
        Long ordemServicoId,
        String numeroOs,
        StatusOrdemServico status,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataTerminoConsiderada,
        Long duracaoSegundos,
        Long duracaoMinutos,
        Boolean emAndamento
) {
}
