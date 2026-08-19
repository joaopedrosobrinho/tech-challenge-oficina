package br.com.fiap.oficina.application.dto.ordemservico;

import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrdemServicoResponse(

        Long id,
        String numeroOs,

        Long clienteId,
        String nomeCliente,
        String cpfCnpjCliente,

        Long veiculoId,
        String placaVeiculo,
        String marcaVeiculo,
        String modeloVeiculo,

        StatusOrdemServico status,
        BigDecimal valorTotal,
        String observacoes,

        LocalDateTime dataCriacao,
        LocalDateTime dataEnvioOrcamento,
        LocalDateTime dataAprovacao,
        LocalDateTime dataInicioExecucao,
        LocalDateTime dataFinalizacao,
        LocalDateTime dataEntrega,
        LocalDateTime dataCancelamento,
        Boolean estoqueBaixado,
        LocalDateTime dataBaixaEstoque,
        Boolean estoqueDevolvido,
        LocalDateTime dataDevolucaoEstoque,

        List<ItemServicoResponse> servicos,
        List<ItemPecaResponse> pecas
) {
}
