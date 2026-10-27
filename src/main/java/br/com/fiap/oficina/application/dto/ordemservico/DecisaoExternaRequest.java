package br.com.fiap.oficina.application.dto.ordemservico;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record DecisaoExternaRequest(
        @NotNull UUID eventoId,
        @NotNull @Positive Long versaoOrcamento,
        @NotNull Boolean aprovada,
        @Size(max = 500) String observacao) {}
