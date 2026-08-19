package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;

import java.time.LocalDateTime;

public record AcompanhamentoOrdemServicoResponse(
        String numeroOs,
        StatusOrdemServico status,
        String etapaAtual,
        LocalDateTime dataCriacao,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFinalizacao,
        LocalDateTime dataEntrega,
        LocalDateTime dataCancelamento
) {
}
