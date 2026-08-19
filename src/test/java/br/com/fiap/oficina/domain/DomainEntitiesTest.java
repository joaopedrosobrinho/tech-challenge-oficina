package br.com.fiap.oficina.domain;

import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import br.com.fiap.oficina.domain.ordemservico.ItemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DomainEntitiesTest {

    @Test
    void deveManterRelacionamentosBidirecionaisDoCliente() {
        Cliente cliente = Cliente.builder().id(1L).nome("Ana").build();
        Veiculo veiculo = Veiculo.builder().id(2L).placa("ABC1D23").build();
        OrdemServico ordem = OrdemServico.builder().id(3L).build();

        cliente.adicionarVeiculo(veiculo);
        cliente.adicionarOrdemServico(ordem);

        assertSame(cliente, veiculo.getCliente());
        assertSame(cliente, ordem.getCliente());
        assertTrue(cliente.getVeiculos().contains(veiculo));
        assertTrue(cliente.getOrdensServico().contains(ordem));

        cliente.removerVeiculo(veiculo);
        assertNull(veiculo.getCliente());
        assertFalse(cliente.getVeiculos().contains(veiculo));
        cliente.removerVeiculo(null);
    }

    @Test
    void deveRejeitarRelacionamentosNulosNoCliente() {
        Cliente cliente = new Cliente();

        assertThrows(IllegalArgumentException.class, () -> cliente.adicionarVeiculo(null));
        assertThrows(IllegalArgumentException.class, () -> cliente.adicionarOrdemServico(null));
    }

    @Test
    void deveAssociarOrdemAoVeiculoERejeitarOrdemNula() {
        Veiculo veiculo = Veiculo.builder().id(1L).build();
        OrdemServico ordem = OrdemServico.builder().id(2L).build();

        veiculo.adicionarOrdemServico(ordem);

        assertSame(veiculo, ordem.getVeiculo());
        assertTrue(veiculo.getOrdensServico().contains(ordem));
        assertThrows(IllegalArgumentException.class, () -> veiculo.adicionarOrdemServico(null));
    }

    @Test
    void deveCalcularEAumentarQuantidadeDoItemServico() {
        ItemServico item = ItemServico.builder()
                .id(1L)
                .servico(Servico.builder().id(1L).build())
                .quantidade(2)
                .valorUnitario(new BigDecimal("12.345"))
                .build();

        assertEquals(new BigDecimal("24.69"), item.calcularSubtotal());
        item.aumentarQuantidade(3);
        assertEquals(5, item.getQuantidade());

        item.setQuantidade(null);
        item.aumentarQuantidade(2);
        assertEquals(2, item.getQuantidade());
        assertThrows(RegraNegocioException.class, () -> item.aumentarQuantidade(0));
        assertThrows(RegraNegocioException.class, () -> item.aumentarQuantidade(null));
    }

    @Test
    void deveCalcularEAumentarQuantidadeDoItemPeca() {
        ItemPeca item = ItemPeca.builder()
                .id(1L)
                .peca(Peca.builder().id(1L).build())
                .quantidade(3)
                .valorUnitario(new BigDecimal("4.999"))
                .build();

        assertEquals(new BigDecimal("15.00"), item.calcularSubtotal());
        item.aumentarQuantidade(2);
        assertEquals(5, item.getQuantidade());

        item.setQuantidade(null);
        item.aumentarQuantidade(1);
        assertEquals(1, item.getQuantidade());
        assertThrows(RegraNegocioException.class, () -> item.aumentarQuantidade(-1));
    }

    @Test
    void deveRetornarZeroParaItensInvalidos() {
        ItemServico servico = new ItemServico();
        ItemPeca peca = new ItemPeca();

        assertEquals(new BigDecimal("0.00"), servico.calcularSubtotal());
        assertEquals(new BigDecimal("0.00"), peca.calcularSubtotal());

        servico.setValorUnitario(BigDecimal.TEN);
        servico.setQuantidade(0);
        peca.setValorUnitario(BigDecimal.TEN);
        peca.setQuantidade(-1);
        assertEquals(new BigDecimal("0.00"), servico.calcularSubtotal());
        assertEquals(new BigDecimal("0.00"), peca.calcularSubtotal());
    }

    @Test
    void igualdadeDosItensDeveUsarIdentidadePersistida() {
        ItemServico servico1 = ItemServico.builder().id(1L).build();
        ItemServico servico2 = ItemServico.builder().id(1L).build();
        ItemPeca peca1 = ItemPeca.builder().id(2L).build();
        ItemPeca peca2 = ItemPeca.builder().id(2L).build();

        assertEquals(servico1, servico1);
        assertEquals(servico1, servico2);
        assertNotEquals(servico1, new ItemServico());
        assertNotEquals(servico1, "serviço");
        assertEquals(servico1.hashCode(), servico2.hashCode());
        assertEquals(peca1, peca1);
        assertEquals(peca1, peca2);
        assertNotEquals(peca1, new ItemPeca());
        assertNotEquals(peca1, "peça");
        assertEquals(peca1.hashCode(), peca2.hashCode());
    }
}
