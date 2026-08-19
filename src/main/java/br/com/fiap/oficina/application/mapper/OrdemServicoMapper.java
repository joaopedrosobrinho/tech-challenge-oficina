package br.com.fiap.oficina.application.mapper;

import br.com.fiap.oficina.application.dto.ordemservico.AcompanhamentoOrdemServicoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.ItemPecaResponse;
import br.com.fiap.oficina.application.dto.ordemservico.ItemServicoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoResponse;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import br.com.fiap.oficina.domain.ordemservico.ItemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;

import java.util.List;

public final class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServicoResponse paraResponse(
            OrdemServico ordemServico
    ) {
        if (ordemServico == null) {
            return null;
        }

        Cliente cliente = ordemServico.getCliente();
        Veiculo veiculo = ordemServico.getVeiculo();

        List<ItemServicoResponse> servicos =
                ordemServico.getItensServico() == null
                        ? List.of()
                        : ordemServico.getItensServico()
                        .stream()
                        .map(OrdemServicoMapper::paraItemServicoResponse)
                        .toList();

        List<ItemPecaResponse> pecas =
                ordemServico.getItensPeca() == null
                        ? List.of()
                        : ordemServico.getItensPeca()
                        .stream()
                        .map(OrdemServicoMapper::paraItemPecaResponse)
                        .toList();

        return new OrdemServicoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),

                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null,
                cliente != null ? cliente.getCpfCnpj() : null,

                veiculo != null ? veiculo.getId() : null,
                veiculo != null ? veiculo.getPlaca() : null,
                veiculo != null ? veiculo.getMarca() : null,
                veiculo != null ? veiculo.getModelo() : null,

                ordemServico.getStatus(),
                ordemServico.getValorTotal(),
                ordemServico.getObservacoes(),

                ordemServico.getDataCriacao(),
                ordemServico.getDataEnvioOrcamento(),
                ordemServico.getDataAprovacao(),
                ordemServico.getDataInicioExecucao(),
                ordemServico.getDataFinalizacao(),
                ordemServico.getDataEntrega(),
                ordemServico.getDataCancelamento(),
                ordemServico.getEstoqueBaixado(),
                ordemServico.getDataBaixaEstoque(),
                ordemServico.getEstoqueDevolvido(),
                ordemServico.getDataDevolucaoEstoque(),

                servicos,
                pecas
        );
    }

    public static AcompanhamentoOrdemServicoResponse
    paraAcompanhamentoPublico(OrdemServico ordemServico) {
        if (ordemServico == null) {
            return null;
        }

        return new AcompanhamentoOrdemServicoResponse(
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                descreverEtapa(ordemServico),
                ordemServico.getDataCriacao(),
                ordemServico.getDataInicioExecucao(),
                ordemServico.getDataFinalizacao(),
                ordemServico.getDataEntrega(),
                ordemServico.getDataCancelamento()
        );
    }

    private static String descreverEtapa(OrdemServico ordemServico) {
        if (ordemServico.getStatus() == null) {
            return "Status indisponível";
        }

        return switch (ordemServico.getStatus()) {
            case RECEBIDA -> "Veículo recebido pela oficina";
            case EM_DIAGNOSTICO -> "Veículo em diagnóstico";
            case AGUARDANDO_APROVACAO -> "Orçamento aguardando aprovação";
            case EM_EXECUCAO -> "Serviços em execução";
            case FINALIZADA -> "Serviços finalizados; veículo aguardando entrega";
            case ENTREGUE -> "Veículo entregue";
            case CANCELADA -> "Ordem de serviço cancelada";
        };
    }

    private static ItemServicoResponse paraItemServicoResponse(
            ItemServico item
    ) {
        return new ItemServicoResponse(
                item.getId(),
                item.getServico() != null
                        ? item.getServico().getId()
                        : null,
                item.getServico() != null
                        ? item.getServico().getNome()
                        : null,
                item.getQuantidade(),
                item.getValorUnitario(),
                item.calcularSubtotal()
        );
    }

    private static ItemPecaResponse paraItemPecaResponse(
            ItemPeca item
    ) {
        return new ItemPecaResponse(
                item.getId(),
                item.getPeca() != null
                        ? item.getPeca().getId()
                        : null,
                item.getPeca() != null
                        ? item.getPeca().getCodigo()
                        : null,
                item.getPeca() != null
                        ? item.getPeca().getNome()
                        : null,
                item.getQuantidade(),
                item.getValorUnitario(),
                item.calcularSubtotal()
        );
    }
}
