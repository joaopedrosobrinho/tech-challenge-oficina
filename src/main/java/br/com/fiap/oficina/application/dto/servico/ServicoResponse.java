package br.com.fiap.oficina.application.dto.servico;

import java.math.BigDecimal;

public record ServicoResponse(
        Long id,
        String nome,
        String descricao,
        BigDecimal valor,
        Integer tempoEstimadoMinutos,
        Boolean ativo
) {
}