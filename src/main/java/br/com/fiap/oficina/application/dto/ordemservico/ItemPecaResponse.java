package br.com.fiap.oficina.application.dto.ordemservico;

import java.math.BigDecimal;

public record ItemPecaResponse(
        Long id,
        Long pecaId,
        String codigoPeca,
        String nomePeca,
        Integer quantidade,
        BigDecimal valorUnitario,
        BigDecimal subtotal
) {
}