package br.com.fiap.oficina.application.dto.veiculo;

public record VeiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        Integer ano,
        Long clienteId,
        String nomeCliente
) {
}