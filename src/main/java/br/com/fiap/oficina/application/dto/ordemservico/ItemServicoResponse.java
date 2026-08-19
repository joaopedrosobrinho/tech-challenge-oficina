package br.com.fiap.oficina.application.dto.ordemservico;

import java.math.BigDecimal;

public record ItemServicoResponse(
        Long id,
        Long servicoId,
        String nomeServico,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {
}