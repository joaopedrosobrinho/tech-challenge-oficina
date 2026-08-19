package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusAprovacaoOrcamento;
import br.com.fiap.oficina.domain.ordemservico.ItemServico;
import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.ItemServicoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.ItemPecaRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarServicoOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarPecaOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoRequest;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private VeiculoService veiculoService;

    @Mock
    private ServicoService servicoService;

    @Mock
    private PecaService pecaService;

    @InjectMocks
    private OrdemServicoService ordemServicoService;

    @Test
    void deveCriarOrdemComRelacionamentosItensEOrcamentoCalculado() {
        Cliente cliente = Cliente.builder().id(10L).nome("Maria").build();
        Veiculo veiculo = Veiculo.builder()
                .id(20L).placa("ABC1D23").cliente(cliente).build();
        Servico servico = Servico.builder()
                .id(30L).nome("Revisão").valor(new BigDecimal("100.00"))
                .ativo(true).build();
        Peca peca = Peca.builder()
                .id(40L).nome("Filtro").valor(new BigDecimal("20.00"))
                .quantidadeEstoque(10).ativo(true).build();
        when(clienteService.buscarEntidadePorId(10L)).thenReturn(cliente);
        when(veiculoService.buscarEntidadePorId(20L)).thenReturn(veiculo);
        when(servicoService.buscarEntidadePorId(30L)).thenReturn(servico);
        when(pecaService.buscarEntidadePorId(40L)).thenReturn(peca);
        when(ordemServicoRepository.save(any())).thenAnswer(invocation -> {
            OrdemServico ordem = invocation.getArgument(0);
            ordem.setId(1L);
            return ordem;
        });
        OrdemServicoRequest request = new OrdemServicoRequest(
                10L,
                20L,
                "  Verificar ruído  ",
                List.of(new ItemServicoRequest(30L, 2)),
                List.of(new ItemPecaRequest(40L, 3))
        );

        var response = ordemServicoService.criar(request);

        assertEquals(1L, response.id());
        assertEquals(10L, response.clienteId());
        assertEquals(20L, response.veiculoId());
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, response.status());
        assertEquals(new BigDecimal("260.00"), response.valorTotal());
        assertEquals("Verificar ruído", response.observacoes());
        assertEquals(1, response.servicos().size());
        assertEquals(1, response.pecas().size());
    }

    @Test
    void naoDeveCriarOrdemQuandoVeiculoNaoPertenceAoCliente() {
        Cliente cliente = Cliente.builder().id(10L).build();
        Cliente outroCliente = Cliente.builder().id(11L).build();
        Veiculo veiculo = Veiculo.builder()
                .id(20L).cliente(outroCliente).build();
        when(clienteService.buscarEntidadePorId(10L)).thenReturn(cliente);
        when(veiculoService.buscarEntidadePorId(20L)).thenReturn(veiculo);
        OrdemServicoRequest request = new OrdemServicoRequest(
                10L, 20L, null, List.of(), List.of()
        );

        assertThrows(RegraNegocioException.class,
                () -> ordemServicoService.criar(request));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveAdicionarEIncrementarServicoRecalculandoTotal() {
        OrdemServico ordem = prepararOrdem(StatusOrdemServico.RECEBIDA);
        Servico servico = Servico.builder()
                .id(30L).nome("Revisão").valor(new BigDecimal("75.00"))
                .ativo(true).build();
        when(servicoService.buscarEntidadePorId(30L)).thenReturn(servico);

        ordemServicoService.adicionarServico(
                1L, new AdicionarServicoOrdemRequest(30L, 1)
        );
        var response = ordemServicoService.adicionarServico(
                1L, new AdicionarServicoOrdemRequest(30L, 2)
        );

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, response.status());
        assertEquals(3, response.servicos().getFirst().quantidade());
        assertEquals(new BigDecimal("225.00"), response.valorTotal());
    }

    @Test
    void naoDeveAdicionarServicoInativo() {
        OrdemServico ordem = OrdemServico.builder()
                .id(1L).numeroOs("OS-TESTE")
                .status(StatusOrdemServico.RECEBIDA).build();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        Servico servico = Servico.builder()
                .id(30L).nome("Inativo").valor(BigDecimal.TEN)
                .ativo(false).build();
        when(servicoService.buscarEntidadePorId(30L)).thenReturn(servico);

        assertThrows(RegraNegocioException.class, () ->
                ordemServicoService.adicionarServico(
                        1L, new AdicionarServicoOrdemRequest(30L, 1)
                ));
    }

    @Test
    void naoDeveAdicionarPecaAcimaDoEstoqueDisponivel() {
        OrdemServico ordem = OrdemServico.builder()
                .id(1L).numeroOs("OS-TESTE")
                .status(StatusOrdemServico.RECEBIDA).build();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        Peca peca = Peca.builder()
                .id(40L).nome("Filtro").valor(BigDecimal.TEN)
                .quantidadeEstoque(2).ativo(true).build();
        when(pecaService.buscarEntidadePorId(40L)).thenReturn(peca);

        assertThrows(RegraNegocioException.class, () ->
                ordemServicoService.adicionarPeca(
                        1L, new AdicionarPecaOrdemRequest(40L, 3)
                ));
    }

    @Test
    void deveEnviarOrcamentoParaAprovacao() {
        OrdemServico ordem = prepararOrdemComOrcamento();

        var response = ordemServicoService.enviarParaAprovacao(1L);

        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO,
                response.statusOrdemServico());
        assertEquals(StatusAprovacaoOrcamento.PENDENTE,
                response.statusAprovacao());
        assertEquals(new BigDecimal("100.00"), response.valorTotal());
        assertEquals(true, ordem.getDataEnvioOrcamento() != null);
    }

    @Test
    void deveAprovarOrcamentoEIniciarExecucao() {
        OrdemServico ordem = prepararOrdemComOrcamento();
        ordem.enviarParaAprovacao();

        var response = ordemServicoService.aprovarOrcamento(
                1L, new AprovacaoOrcamentoRequest("  Aprovado por telefone  ")
        );

        assertEquals(StatusOrdemServico.EM_EXECUCAO,
                response.statusOrdemServico());
        assertEquals(StatusAprovacaoOrcamento.APROVADO,
                response.statusAprovacao());
        assertEquals("Aprovado por telefone", response.observacao());
        assertEquals(true, ordem.getDataInicioExecucao() != null);
        assertEquals(true, ordem.getDataAprovacao() != null);
    }

    @Test
    void deveExigirObservacaoParaRecusarOrcamento() {
        OrdemServico ordem = ordemComOrcamentoSemStubDeSave();
        ordem.enviarParaAprovacao();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));

        assertThrows(RegraNegocioException.class, () ->
                ordemServicoService.recusarOrcamento(
                        1L, new AprovacaoOrcamentoRequest("   ")
                ));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void naoDeveAprovarOrcamentoForaDeAguardandoAprovacao() {
        OrdemServico ordem = ordemComOrcamentoSemStubDeSave();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));

        assertThrows(RegraNegocioException.class, () ->
                ordemServicoService.aprovarOrcamento(1L, null));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveRecusarRevisarEReenviarOrcamento() {
        OrdemServico ordem = prepararOrdemComOrcamento();
        ordemServicoService.enviarParaAprovacao(1L);

        var recusado = ordemServicoService.recusarOrcamento(
                1L, new AprovacaoOrcamentoRequest("  Valor acima do esperado  ")
        );
        var reenviado = ordemServicoService.enviarParaAprovacao(1L);

        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO,
                recusado.statusOrdemServico());
        assertEquals(StatusAprovacaoOrcamento.RECUSADO,
                recusado.statusAprovacao());
        assertEquals("Valor acima do esperado", recusado.observacao());
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO,
                reenviado.statusOrdemServico());
        assertEquals(StatusAprovacaoOrcamento.PENDENTE,
                reenviado.statusAprovacao());
        assertEquals(null, reenviado.observacao());
    }

    @Test
    void naoDeveEnviarNovamenteEnquantoJaAguardaAprovacao() {
        OrdemServico ordem = ordemComOrcamentoSemStubDeSave();
        ordem.enviarParaAprovacao();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));

        assertThrows(RegraNegocioException.class,
                () -> ordemServicoService.enviarParaAprovacao(1L));
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void deveBaixarEstoqueDaPecaAoAprovarOrcamento() {
        Peca peca = Peca.builder()
                .id(40L).nome("Filtro").valor(new BigDecimal("25.00"))
                .quantidadeEstoque(10).ativo(true).build();
        OrdemServico ordem = ordemComPecaAguardandoAprovacao(peca, 3);
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);

        ordemServicoService.aprovarOrcamento(1L, null);

        assertEquals(7, peca.getQuantidadeEstoque());
        assertEquals(true, ordem.getEstoqueBaixado());
        assertEquals(true, ordem.getDataBaixaEstoque() != null);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, ordem.getStatus());
    }

    @Test
    void naoDeveAlterarEstoqueQuandoQuantidadeForInsuficiente() {
        Peca primeira = Peca.builder()
                .id(40L).nome("Filtro").valor(BigDecimal.TEN)
                .quantidadeEstoque(10).ativo(true).build();
        Peca insuficiente = Peca.builder()
                .id(41L).nome("Pastilha").valor(BigDecimal.TEN)
                .quantidadeEstoque(1).ativo(true).build();
        OrdemServico ordem = ordemComPecaAguardandoAprovacao(primeira, 3);
        ordem.setStatus(StatusOrdemServico.EM_DIAGNOSTICO);
        ordem.adicionarPeca(ItemPeca.builder()
                .peca(insuficiente).quantidade(2)
                .valorUnitario(BigDecimal.TEN).build());
        ordem.enviarParaAprovacao();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));

        assertThrows(RegraNegocioException.class,
                () -> ordemServicoService.aprovarOrcamento(1L, null));

        assertEquals(10, primeira.getQuantidadeEstoque());
        assertEquals(1, insuficiente.getQuantidadeEstoque());
        assertEquals(false, ordem.getEstoqueBaixado());
        verify(ordemServicoRepository, never()).save(any());
    }

    @Test
    void naoDeveRealizarDuplaBaixaDeEstoque() {
        Peca peca = Peca.builder()
                .id(40L).nome("Filtro").valor(BigDecimal.TEN)
                .quantidadeEstoque(5).ativo(true).build();
        OrdemServico ordem = ordemComPecaAguardandoAprovacao(peca, 2);
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);

        ordemServicoService.aprovarOrcamento(1L, null);
        assertThrows(RegraNegocioException.class,
                () -> ordemServicoService.aprovarOrcamento(1L, null));

        assertEquals(3, peca.getQuantidadeEstoque());
    }

    @Test
    void deveDevolverEstoqueUmaUnicaVezAoCancelarExecucao() {
        Peca peca = Peca.builder()
                .id(40L).nome("Filtro").valor(BigDecimal.TEN)
                .quantidadeEstoque(5).ativo(true).build();
        OrdemServico ordem = ordemComPecaAguardandoAprovacao(peca, 2);
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);

        ordemServicoService.aprovarOrcamento(1L, null);
        var response = ordemServicoService.cancelar(1L);

        assertEquals(5, peca.getQuantidadeEstoque());
        assertEquals(StatusOrdemServico.CANCELADA, response.status());
        assertEquals(true, response.estoqueBaixado());
        assertEquals(true, response.estoqueDevolvido());
        assertEquals(true, response.dataDevolucaoEstoque() != null);
        assertThrows(RegraNegocioException.class,
                () -> ordemServicoService.cancelar(1L));
        assertEquals(5, peca.getQuantidadeEstoque());
    }

    @Test
    void deveFinalizarOrdemEmExecucao() {
        OrdemServico ordemServico = prepararOrdem(StatusOrdemServico.EM_EXECUCAO);

        ordemServicoService.finalizar(1L);

        assertEquals(StatusOrdemServico.FINALIZADA, ordemServico.getStatus());
    }

    @Test
    void deveEntregarOrdemFinalizada() {
        OrdemServico ordemServico = prepararOrdem(StatusOrdemServico.FINALIZADA);

        ordemServicoService.entregar(1L);

        assertEquals(StatusOrdemServico.ENTREGUE, ordemServico.getStatus());
    }

    @Test
    void deveCancelarOrdemNaoEntregue() {
        OrdemServico ordemServico = prepararOrdem(StatusOrdemServico.RECEBIDA);

        ordemServicoService.cancelar(1L);

        assertEquals(StatusOrdemServico.CANCELADA, ordemServico.getStatus());
    }

    @Test
    void deveListarOrdensPorCliente() {
        when(ordemServicoRepository
                .findAllByClienteIdOrderByDataCriacaoDesc(10L))
                .thenReturn(List.of());

        assertEquals(0, ordemServicoService.listarPorCliente(10L).size());
    }

    @Test
    void deveListarOrdensPorVeiculo() {
        when(ordemServicoRepository
                .findAllByVeiculoIdOrderByDataCriacaoDesc(20L))
                .thenReturn(List.of());

        assertEquals(0, ordemServicoService.listarPorVeiculo(20L).size());
    }

    @Test
    void deveListarOrdensPorStatus() {
        when(ordemServicoRepository
                .findAllByStatusOrderByDataCriacaoDesc(
                        StatusOrdemServico.EM_EXECUCAO
                ))
                .thenReturn(List.of());

        assertEquals(
                0,
                ordemServicoService
                        .listarPorStatus(StatusOrdemServico.EM_EXECUCAO)
                        .size()
        );
    }

    @Test
    void devePermitirAcompanhamentoPublicoComNumeroEPlacaCorretos() {
        OrdemServico ordemServico = OrdemServico.builder()
                .numeroOs("OS-123")
                .status(StatusOrdemServico.EM_DIAGNOSTICO)
                .veiculo(Veiculo.builder().placa("ABC1D23").build())
                .build();
        when(ordemServicoRepository.findByNumeroOs("OS-123"))
                .thenReturn(Optional.of(ordemServico));

        var resposta = ordemServicoService
                .acompanharPublicamente("os-123", "abc-1d23");

        assertEquals("OS-123", resposta.numeroOs());
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, resposta.status());
    }

    @Test
    void naoDeveExporAcompanhamentoQuandoPlacaNaoCorresponde() {
        OrdemServico ordemServico = OrdemServico.builder()
                .numeroOs("OS-123")
                .status(StatusOrdemServico.EM_DIAGNOSTICO)
                .veiculo(Veiculo.builder().placa("ABC1D23").build())
                .build();
        when(ordemServicoRepository.findByNumeroOs("OS-123"))
                .thenReturn(Optional.of(ordemServico));

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> ordemServicoService
                        .acompanharPublicamente("OS-123", "ZZZ9Z99")
        );
    }

    @Test
    void deveCalcularDuracaoDeOrdemFinalizada() {
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 18, 10, 0);
        OrdemServico ordemServico = OrdemServico.builder()
                .id(1L)
                .numeroOs("OS-123")
                .status(StatusOrdemServico.FINALIZADA)
                .dataInicioExecucao(inicio)
                .dataFinalizacao(inicio.plusMinutes(90))
                .build();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordemServico));

        var resposta = ordemServicoService.consultarDuracaoExecucao(1L);

        assertEquals(90L, resposta.duracaoMinutos());
        assertEquals(5400L, resposta.duracaoSegundos());
        assertEquals(false, resposta.emAndamento());
    }

    @Test
    void naoDeveCalcularDuracaoAntesDoInicioDaExecucao() {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(1L)
                .numeroOs("OS-123")
                .status(StatusOrdemServico.EM_DIAGNOSTICO)
                .build();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordemServico));

        assertThrows(
                br.com.fiap.oficina.application.exception.RegraNegocioException.class,
                () -> ordemServicoService.consultarDuracaoExecucao(1L)
        );
    }

    @Test
    void deveCalcularTempoMedioSomenteComOrdensFinalizadas() {
        LocalDateTime inicio = LocalDateTime.of(2026, 8, 18, 10, 0);
        OrdemServico primeira = OrdemServico.builder()
                .dataInicioExecucao(inicio)
                .dataFinalizacao(inicio.plusMinutes(60))
                .build();
        OrdemServico segunda = OrdemServico.builder()
                .dataInicioExecucao(inicio)
                .dataFinalizacao(inicio.plusMinutes(120))
                .build();
        when(ordemServicoRepository
                .findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull())
                .thenReturn(List.of(primeira, segunda));

        var resposta = ordemServicoService.consultarTempoMedioExecucao();

        assertEquals(2, resposta.quantidadeOrdensConsideradas());
        assertEquals(new BigDecimal("90.00"), resposta.tempoMedioMinutos());
        assertEquals(new BigDecimal("5400.00"), resposta.tempoMedioSegundos());
    }

    @Test
    void deveRetornarMediaZeroQuandoNaoHaOrdensFinalizadas() {
        when(ordemServicoRepository
                .findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull())
                .thenReturn(List.of());

        var resposta = ordemServicoService.consultarTempoMedioExecucao();

        assertEquals(0, resposta.quantidadeOrdensConsideradas());
        assertEquals(new BigDecimal("0.00"), resposta.tempoMedioMinutos());
    }

    private OrdemServico prepararOrdem(StatusOrdemServico status) {
        OrdemServico ordemServico = OrdemServico.builder()
                .id(1L)
                .numeroOs("OS-TESTE")
                .status(status)
                .build();

        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordemServico));
        when(ordemServicoRepository.save(ordemServico))
                .thenReturn(ordemServico);

        return ordemServico;
    }

    private OrdemServico prepararOrdemComOrcamento() {
        OrdemServico ordem = ordemComOrcamentoSemStubDeSave();
        when(ordemServicoRepository.findOneById(1L))
                .thenReturn(Optional.of(ordem));
        when(ordemServicoRepository.save(ordem)).thenReturn(ordem);
        return ordem;
    }

    private OrdemServico ordemComOrcamentoSemStubDeSave() {
        OrdemServico ordem = OrdemServico.builder()
                .id(1L)
                .numeroOs("OS-APROVACAO")
                .status(StatusOrdemServico.RECEBIDA)
                .build();
        Servico servico = Servico.builder()
                .id(30L)
                .nome("Revisão")
                .valor(new BigDecimal("100.00"))
                .ativo(true)
                .build();
        ordem.adicionarServico(ItemServico.builder()
                .servico(servico)
                .quantidade(1)
                .valorUnitario(servico.getValor())
                .build());
        return ordem;
    }

    private OrdemServico ordemComPecaAguardandoAprovacao(
            Peca peca,
            int quantidade
    ) {
        OrdemServico ordem = ordemComOrcamentoSemStubDeSave();
        ordem.adicionarPeca(ItemPeca.builder()
                .peca(peca)
                .quantidade(quantidade)
                .valorUnitario(peca.getValor())
                .build());
        ordem.enviarParaAprovacao();
        return ordem;
    }
}
