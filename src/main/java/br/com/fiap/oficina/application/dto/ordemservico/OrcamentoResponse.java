package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrcamentoResponse(

        Long ordemServicoId,
        String numeroOs,
        StatusOrdemServico status,

        Integer quantidadeServicos,
        Integer quantidadePecas,

        BigDecimal totalServicos,
        BigDecimal totalPecas,
        BigDecimal valorTotal,

        LocalDateTime calculadoEm
) {
}