package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OrdemServicoTest {

    @Test
    void deveManterMultiplosItensTransitoriosNosSets() {
        OrdemServico ordemServico = novaOrdem();

        ordemServico.adicionarServico(novoItemServico("Troca de óleo"));
        ordemServico.adicionarServico(novoItemServico("Alinhamento"));
        ordemServico.adicionarPeca(novoItemPeca("Filtro"));
        ordemServico.adicionarPeca(novoItemPeca("Óleo"));

        assertEquals(2, ordemServico.getItensServico().size());
        assertEquals(2, ordemServico.getItensPeca().size());
        assertEquals(new BigDecimal("40.00"), ordemServico.getValorTotal());
    }

    @Test
    void deveExecutarFluxoAutomaticoAteEntrega() {
        OrdemServico ordemServico = novaOrdem();

        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordemServico.getStatus());

        ordemServico.enviarParaAprovacao();
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordemServico.getStatus());

        ordemServico.aprovarOrcamentoAutomaticamente(null);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataInicioExecucao());

        ordemServico.finalizarExecucao();
        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataFinalizacao());

        ordemServico.entregarVeiculo();
        assertEquals(StatusOrdemServico.ENTREGUE, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataEntrega());
    }

    @Test
    void deveImpedirAumentoDeItemEnquantoAguardaAprovacao() {
        OrdemServico ordemServico = novaOrdem();
        ItemServico itemServico = novoItemServico("Diagnóstico");

        ordemServico.adicionarServico(itemServico);
        ordemServico.enviarParaAprovacao();

        assertThrows(
                RegraNegocioException.class,
                () -> ordemServico.aumentarQuantidadeServico(itemServico, 1)
        );
    }

    @Test
    void deveExigirObservacaoAoRecusarOrcamento() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.enviarParaAprovacao();

        assertThrows(
                RegraNegocioException.class,
                () -> ordemServico.recusarOrcamentoAutomaticamente(" ")
        );
    }

    @Test
    void deveVoltarAoDiagnosticoQuandoOrcamentoForRecusado() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.enviarParaAprovacao();

        ordemServico.recusarOrcamentoAutomaticamente("Cliente solicitou revisão");

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordemServico.getStatus());
        assertEquals(
                StatusAprovacaoOrcamento.RECUSADO,
                ordemServico.getStatusAprovacaoOrcamento()
        );
    }

    @Test
    void devePermitirCancelarAntesDaEntrega() {
        OrdemServico ordemServico = novaOrdem();

        ordemServico.cancelar();

        assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
        assertNotNull(ordemServico.getDataCancelamento());
    }

    @Test
    void naoDevePermitirCancelarDepoisDaEntrega() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.enviarParaAprovacao();
        ordemServico.aprovarOrcamentoAutomaticamente(null);
        ordemServico.finalizarExecucao();
        ordemServico.entregarVeiculo();

        assertThrows(RegraNegocioException.class, ordemServico::cancelar);
    }

    @Test
    void naoDevePermitirCancelarOrdemFinalizada() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.setStatus(StatusOrdemServico.FINALIZADA);

        assertThrows(RegraNegocioException.class, ordemServico::cancelar);
    }

    @Test
    void naoDevePermitirCancelarDuasVezes() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.cancelar();

        assertThrows(RegraNegocioException.class, ordemServico::cancelar);
    }

    @Test
    void naoDeveFinalizarOrdemForaDeExecucao() {
        OrdemServico ordemServico = novaOrdem();

        assertThrows(
                RegraNegocioException.class,
                ordemServico::finalizarExecucao
        );
    }

    @Test
    void naoDeveEntregarOrdemNaoFinalizada() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);

        assertThrows(
                RegraNegocioException.class,
                ordemServico::entregarVeiculo
        );
    }

    @Test
    void deveBaixarEstoqueAoAprovarOrcamento() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = Peca.builder()
                .nome("Filtro")
                .valor(new BigDecimal("10.00"))
                .quantidadeEstoque(5)
                .build();

        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.adicionarPeca(ItemPeca.builder()
                .peca(peca)
                .quantidade(2)
                .valorUnitario(peca.getValor())
                .build());
        ordemServico.enviarParaAprovacao();

        ordemServico.aprovarOrcamentoAutomaticamente(null);

        assertEquals(3, peca.getQuantidadeEstoque());
        assertEquals(true, ordemServico.getEstoqueBaixado());
        assertNotNull(ordemServico.getDataBaixaEstoque());
    }

    @Test
    void naoDeveIniciarExecucaoComEstoqueInsuficiente() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = Peca.builder()
                .nome("Filtro")
                .valor(new BigDecimal("10.00"))
                .quantidadeEstoque(1)
                .build();

        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.adicionarPeca(ItemPeca.builder()
                .peca(peca)
                .quantidade(2)
                .valorUnitario(peca.getValor())
                .build());
        ordemServico.enviarParaAprovacao();

        assertThrows(
                RegraNegocioException.class,
                () -> ordemServico.aprovarOrcamentoAutomaticamente(null)
        );
        assertEquals(1, peca.getQuantidadeEstoque());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordemServico.getStatus());
        assertEquals(false, ordemServico.getEstoqueBaixado());
    }

    @Test
    void deveRegistrarBaixaMesmoQuandoOrdemNaoPossuiPecas() {
        OrdemServico ordemServico = novaOrdem();
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.enviarParaAprovacao();

        ordemServico.aprovarOrcamentoAutomaticamente(null);

        assertEquals(true, ordemServico.getEstoqueBaixado());
        assertNotNull(ordemServico.getDataBaixaEstoque());
    }

    @Test
    void deveDevolverEstoqueAoCancelarOrdemEmExecucao() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = novaPecaComEstoque("Filtro", 5);
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.adicionarPeca(novoItemPeca(peca, 2));
        ordemServico.enviarParaAprovacao();
        ordemServico.aprovarOrcamentoAutomaticamente(null);

        ordemServico.cancelar();

        assertEquals(5, peca.getQuantidadeEstoque());
        assertEquals(true, ordemServico.getEstoqueDevolvido());
        assertNotNull(ordemServico.getDataDevolucaoEstoque());
    }

    @Test
    void naoDeveDevolverEstoqueQuandoCancelamentoOcorreAntesDaExecucao() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = novaPecaComEstoque("Filtro", 5);
        ordemServico.adicionarPeca(novoItemPeca(peca, 2));

        ordemServico.cancelar();

        assertEquals(5, peca.getQuantidadeEstoque());
        assertEquals(false, ordemServico.getEstoqueDevolvido());
    }

    @Test
    void deveRemoverPecaAntesDaExecucaoSemAlterarEstoque() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = novaPecaComEstoque("Filtro", 5);
        ItemPeca itemPeca = novoItemPeca(peca, 2);
        ordemServico.adicionarPeca(itemPeca);

        ordemServico.removerPeca(itemPeca);

        assertEquals(0, ordemServico.getItensPeca().size());
        assertEquals(5, peca.getQuantidadeEstoque());
    }

    @Test
    void deveValidarQuantidadeTotalDeItensRepetidosAntesDaBaixa() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = novaPecaComEstoque("Filtro", 5);
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.adicionarPeca(novoItemPeca(peca, 3));
        ordemServico.adicionarPeca(novoItemPeca(peca, 3));
        ordemServico.enviarParaAprovacao();

        assertThrows(
                RegraNegocioException.class,
                () -> ordemServico.aprovarOrcamentoAutomaticamente(null)
        );
        assertEquals(5, peca.getQuantidadeEstoque());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordemServico.getStatus());
    }

    @Test
    void deveExecutarFluxoComRecusaRevisaoAprovacaoECancelamento() {
        OrdemServico ordemServico = novaOrdem();
        Peca peca = novaPecaComEstoque("Filtro", 4);
        ItemPeca itemPeca = novoItemPeca(peca, 1);
        ordemServico.adicionarServico(novoItemServico("Diagnóstico"));
        ordemServico.adicionarPeca(itemPeca);
        ordemServico.enviarParaAprovacao();
        ordemServico.recusarOrcamentoAutomaticamente("Revisar orçamento");

        ordemServico.aumentarQuantidadePeca(itemPeca, 1);
        ordemServico.enviarParaAprovacao();
        ordemServico.aprovarOrcamentoAutomaticamente("Aprovado após revisão");

        assertEquals(2, peca.getQuantidadeEstoque());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordemServico.getStatus());

        ordemServico.cancelar();

        assertEquals(4, peca.getQuantidadeEstoque());
        assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
    }

    @Test
    void deveInicializarEValidarOrdemAntesDeSalvarEAtualizar() {
        Cliente cliente = Cliente.builder().id(1L).build();
        Veiculo veiculo = Veiculo.builder().id(2L).cliente(cliente).build();
        OrdemServico ordem = OrdemServico.builder()
                .numeroOs("OS-1")
                .cliente(cliente)
                .veiculo(veiculo)
                .statusAprovacaoOrcamento(null)
                .itensServico(null)
                .itensPeca(null)
                .build();

        ordem.antesDeSalvar();
        ordem.antesDeAtualizar();

        assertNotNull(ordem.getDataCriacao());
        assertEquals(StatusOrdemServico.RECEBIDA, ordem.getStatus());
        assertEquals(StatusAprovacaoOrcamento.PENDENTE, ordem.getStatusAprovacaoOrcamento());
        assertNotNull(ordem.getItensServico());
        assertNotNull(ordem.getItensPeca());
    }

    @Test
    void deveValidarDadosObrigatoriosAntesDePersistir() {
        OrdemServico ordem = new OrdemServico();
        assertThrows(RegraNegocioException.class, ordem::antesDeSalvar);

        ordem.setNumeroOs("OS-1");
        assertThrows(RegraNegocioException.class, ordem::antesDeAtualizar);

        ordem.setCliente(Cliente.builder().id(1L).build());
        assertThrows(RegraNegocioException.class, ordem::antesDeAtualizar);
    }

    @Test
    void deveValidarPropriedadeDoVeiculo() {
        Cliente cliente = Cliente.builder().id(1L).build();
        Cliente outroCliente = Cliente.builder().id(2L).build();
        Veiculo semCliente = Veiculo.builder().build();
        Veiculo deOutroCliente = Veiculo.builder().cliente(outroCliente).build();
        OrdemServico ordem = novaOrdem();

        assertThrows(RegraNegocioException.class, () -> ordem.definirClienteEVeiculo(null, semCliente));
        assertThrows(RegraNegocioException.class, () -> ordem.definirClienteEVeiculo(cliente, null));
        assertThrows(RegraNegocioException.class, () -> ordem.definirClienteEVeiculo(cliente, semCliente));
        assertThrows(RegraNegocioException.class, () -> ordem.definirClienteEVeiculo(cliente, deOutroCliente));

        Veiculo correto = Veiculo.builder().cliente(cliente).build();
        ordem.definirClienteEVeiculo(cliente, correto);
        assertEquals(cliente, ordem.getCliente());
        assertEquals(correto, ordem.getVeiculo());
    }

    @Test
    void deveRemoverServicoEIgnorarRemocoesAusentes() {
        OrdemServico ordem = novaOrdem();
        ItemServico item = novoItemServico("Diagnóstico");
        ordem.adicionarServico(item);

        ordem.removerServico(item);
        ordem.removerServico(item);
        ordem.removerServico(null);

        assertTrue(ordem.getItensServico().isEmpty());
        assertNull(item.getOrdemServico());
        assertEquals(new BigDecimal("0.00"), ordem.getValorTotal());
    }

    @Test
    void deveValidarItensAoAdicionarEAumentar() {
        OrdemServico ordem = novaOrdem();
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarServico(null));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarServico(new ItemServico()));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarServico(
                ItemServico.builder().servico(new Servico()).quantidade(0).build()));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarPeca(null));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarPeca(new ItemPeca()));
        assertThrows(RegraNegocioException.class, () -> ordem.adicionarPeca(
                ItemPeca.builder().peca(new Peca()).quantidade(0).build()));
        assertThrows(RegraNegocioException.class, () -> ordem.aumentarQuantidadeServico(null, 1));
        assertThrows(RegraNegocioException.class, () -> ordem.aumentarQuantidadePeca(null, 1));
    }

    @Test
    void deveBuscarItensPorIdentificador() {
        OrdemServico ordem = novaOrdem();
        ItemServico servico = novoItemServico("Diagnóstico");
        servico.getServico().setId(10L);
        ItemPeca peca = novoItemPeca(novaPecaComEstoque("Filtro", 2), 1);
        peca.getPeca().setId(20L);
        ordem.adicionarServico(servico);
        ordem.adicionarPeca(peca);

        assertEquals(servico, ordem.buscarItemServicoPorServicoId(10L));
        assertNull(ordem.buscarItemServicoPorServicoId(99L));
        assertEquals(peca, ordem.buscarItemPecaPorPecaId(20L));
        assertNull(ordem.buscarItemPecaPorPecaId(99L));
    }

    @Test
    void deveInformarEstadosEPermissaoDeAlteracao() {
        OrdemServico ordem = novaOrdem();
        assertTrue(ordem.podeAlterarOrcamento());
        assertFalse(ordem.estaEmExecucao());

        ordem.setStatus(StatusOrdemServico.EM_EXECUCAO);
        assertTrue(ordem.estaEmExecucao());
        assertFalse(ordem.podeAlterarOrcamento());
        ordem.setStatus(StatusOrdemServico.FINALIZADA);
        assertTrue(ordem.estaFinalizada());
        ordem.setStatus(StatusOrdemServico.ENTREGUE);
        assertTrue(ordem.estaEntregue());
    }

    @Test
    void deveImpedirAlteracaoDeItensEmEstadosFechados() {
        for (StatusOrdemServico status : new StatusOrdemServico[]{
                StatusOrdemServico.EM_EXECUCAO,
                StatusOrdemServico.FINALIZADA,
                StatusOrdemServico.ENTREGUE,
                StatusOrdemServico.CANCELADA}) {
            OrdemServico ordem = novaOrdem();
            ordem.setStatus(status);
            assertThrows(RegraNegocioException.class,
                    () -> ordem.adicionarServico(novoItemServico("Teste")));
        }
    }

    @Test
    void deveRejeitarEnvioEAprovacaoEmEstadosInvalidos() {
        OrdemServico ordem = novaOrdem();
        assertThrows(RegraNegocioException.class, ordem::enviarParaAprovacao);

        ordem.setStatus(StatusOrdemServico.FINALIZADA);
        assertThrows(RegraNegocioException.class, ordem::enviarParaAprovacao);
        assertThrows(RegraNegocioException.class, () -> ordem.aprovarOrcamentoAutomaticamente(null));
        assertThrows(RegraNegocioException.class, () -> ordem.recusarOrcamentoAutomaticamente("motivo"));
    }

    @Test
    void deveNormalizarObservacaoAoAprovar() {
        OrdemServico ordem = novaOrdem();
        ordem.adicionarServico(novoItemServico("Diagnóstico"));
        ordem.enviarParaAprovacao();
        ordem.aprovarOrcamentoAutomaticamente("  aprovado  ");

        assertEquals("aprovado", ordem.getObservacaoAprovacaoOrcamento());
        assertNotNull(ordem.getDataAprovacao());
        assertNotNull(ordem.getDataAprovacaoOrcamento());
    }

    private Peca novaPecaComEstoque(String nome, Integer estoque) {
        return Peca.builder()
                .nome(nome)
                .valor(new BigDecimal("10.00"))
                .quantidadeEstoque(estoque)
                .build();
    }

    private ItemPeca novoItemPeca(Peca peca, Integer quantidade) {
        return ItemPeca.builder()
                .peca(peca)
                .quantidade(quantidade)
                .valorUnitario(peca.getValor())
                .build();
    }

    private OrdemServico novaOrdem() {
        return OrdemServico.builder()
                .numeroOs("OS-TESTE")
                .status(StatusOrdemServico.RECEBIDA)
                .build();
    }

    private ItemServico novoItemServico(String nome) {
        Servico servico = Servico.builder()
                .nome(nome)
                .valor(new BigDecimal("10.00"))
                .build();

        return ItemServico.builder()
                .servico(servico)
                .quantidade(1)
                .valorUnitario(servico.getValor())
                .build();
    }

    private ItemPeca novoItemPeca(String nome) {
        Peca peca = Peca.builder()
                .nome(nome)
                .valor(new BigDecimal("10.00"))
                .build();

        return ItemPeca.builder()
                .peca(peca)
                .quantidade(1)
                .valorUnitario(peca.getValor())
                .build();
    }
}
