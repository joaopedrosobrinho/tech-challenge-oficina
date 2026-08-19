package br.com.fiap.oficina.application.dto.ordemservico;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TempoMedioExecucaoResponse(
        Integer quantidadeOrdensConsideradas,
        BigDecimal tempoMedioSegundos,
        BigDecimal tempoMedioMinutos,
        LocalDateTime calculadoEm
) {
}
