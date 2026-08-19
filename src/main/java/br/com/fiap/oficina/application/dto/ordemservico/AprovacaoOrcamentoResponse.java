package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusAprovacaoOrcamento;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AprovacaoOrcamentoResponse(

        Long ordemServicoId,
        String numeroOs,
        StatusOrdemServico statusOrdemServico,
        StatusAprovacaoOrcamento statusAprovacao,
        BigDecimal valorTotal,
        LocalDateTime dataAprovacao,
        String observacao

) {
}