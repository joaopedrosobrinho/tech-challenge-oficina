package br.com.fiap.oficina.application.dto.peca;

import java.math.BigDecimal;

public record PecaResponse(
        Long id,
        String codigo,
        String nome,
        String descricao,
        BigDecimal valor,
        Integer quantidadeEstoque,
        Boolean ativo
) {
}