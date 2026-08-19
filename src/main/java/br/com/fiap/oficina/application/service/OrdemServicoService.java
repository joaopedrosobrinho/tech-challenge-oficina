package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.ordemservico.AdicionarPecaOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarServicoOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AcompanhamentoOrdemServicoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.DuracaoExecucaoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.ItemPecaRequest;
import br.com.fiap.oficina.application.dto.ordemservico.ItemServicoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.OrcamentoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.TempoMedioExecucaoResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.mapper.OrdemServicoMapper;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import br.com.fiap.oficina.domain.ordemservico.ItemServico;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

    private static final DateTimeFormatter FORMATO_NUMERO_OS =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final OrdemServicoRepository ordemServicoRepository;
    private final ClienteService clienteService;
    private final VeiculoService veiculoService;
    private final ServicoService servicoService;
    private final PecaService pecaService;

    @Transactional
    public OrdemServicoResponse criar(
            OrdemServicoRequest request
    ) {
        Cliente cliente = clienteService.buscarEntidadePorId(
                request.clienteId()
        );

        Veiculo veiculo = veiculoService.buscarEntidadePorId(
                request.veiculoId()
        );

        validarVeiculoPertenceAoCliente(cliente, veiculo);

        OrdemServico ordemServico = OrdemServico.builder()
                .numeroOs(gerarNumeroOs())
                .cliente(cliente)
                .veiculo(veiculo)
                .status(StatusOrdemServico.RECEBIDA)
                .observacoes(normalizarObservacoes(request.observacoes()))
                .dataCriacao(LocalDateTime.now())
                .build();

        adicionarServicos(
                ordemServico,
                request.servicos()
        );

        adicionarPecas(
                ordemServico,
                request.pecas()
        );

        ordemServico.recalcularValorTotal();

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return OrdemServicoMapper.paraResponse(ordemSalva);
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarTodas() {
        return ordemServicoRepository.buscarTodasComDetalhes()
                .stream()
                .map(OrdemServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorCliente(Long clienteId) {
        clienteService.buscarEntidadePorId(clienteId);

        return ordemServicoRepository
                .findAllByClienteIdOrderByDataCriacaoDesc(clienteId)
                .stream()
                .map(OrdemServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorVeiculo(Long veiculoId) {
        veiculoService.buscarEntidadePorId(veiculoId);

        return ordemServicoRepository
                .findAllByVeiculoIdOrderByDataCriacaoDesc(veiculoId)
                .stream()
                .map(OrdemServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorStatus(
            StatusOrdemServico status
    ) {
        if (status == null) {
            throw new RegraNegocioException(
                    "O status da ordem de serviço é obrigatório"
            );
        }

        return ordemServicoRepository
                .findAllByStatusOrderByDataCriacaoDesc(status)
                .stream()
                .map(OrdemServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(Long id) {
        return OrdemServicoMapper.paraResponse(
                buscarEntidadePorId(id)
        );
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorNumero(
            String numeroOs
    ) {
        OrdemServico ordemServico = ordemServicoRepository
                .findByNumeroOs(numeroOs.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ordem de serviço não encontrada com o número: "
                                + numeroOs
                ));

        return OrdemServicoMapper.paraResponse(ordemServico);
    }

    @Transactional(readOnly = true)
    public AcompanhamentoOrdemServicoResponse acompanharPublicamente(
            String numeroOs,
            String placa
    ) {
        if (numeroOs == null || numeroOs.isBlank()
                || placa == null || placa.isBlank()) {
            throw new RegraNegocioException(
                    "O número da OS e a placa do veículo são obrigatórios"
            );
        }

        OrdemServico ordemServico = ordemServicoRepository
                .findByNumeroOs(numeroOs.trim().toUpperCase())
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ordem de serviço não encontrada"
                ));

        String placaInformada = placa
                .replaceAll("[^A-Za-z0-9]", "")
                .toUpperCase();
        String placaDaOrdem = ordemServico.getVeiculo() == null
                ? null
                : ordemServico.getVeiculo().getPlaca();

        if (placaDaOrdem == null
                || !placaDaOrdem.equalsIgnoreCase(placaInformada)) {
            throw new RecursoNaoEncontradoException(
                    "Ordem de serviço não encontrada"
            );
        }

        return OrdemServicoMapper.paraAcompanhamentoPublico(ordemServico);
    }

    @Transactional(readOnly = true)
    public DuracaoExecucaoResponse consultarDuracaoExecucao(
            Long ordemServicoId
    ) {
        OrdemServico ordemServico = buscarEntidadePorId(ordemServicoId);
        LocalDateTime inicio = ordemServico.getDataInicioExecucao();

        if (inicio == null) {
            throw new RegraNegocioException(
                    "A ordem de serviço ainda não iniciou a execução"
            );
        }

        boolean emAndamento = ordemServico.getStatus()
                == StatusOrdemServico.EM_EXECUCAO;
        LocalDateTime termino = ordemServico.getDataFinalizacao();

        if (termino == null
                && ordemServico.getStatus() == StatusOrdemServico.CANCELADA) {
            termino = ordemServico.getDataCancelamento();
        }

        if (termino == null) {
            termino = LocalDateTime.now();
        }

        if (termino.isBefore(inicio)) {
            throw new RegraNegocioException(
                    "As datas de execução da ordem de serviço são inconsistentes"
            );
        }

        Duration duracao = Duration.between(inicio, termino);

        return new DuracaoExecucaoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                inicio,
                termino,
                duracao.getSeconds(),
                duracao.toMinutes(),
                emAndamento
        );
    }

    @Transactional(readOnly = true)
    public TempoMedioExecucaoResponse consultarTempoMedioExecucao() {
        List<OrdemServico> ordensFinalizadas = ordemServicoRepository
                .findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull();

        long totalSegundos = 0;

        for (OrdemServico ordemServico : ordensFinalizadas) {
            LocalDateTime inicio = ordemServico.getDataInicioExecucao();
            LocalDateTime fim = ordemServico.getDataFinalizacao();

            if (fim.isBefore(inicio)) {
                throw new RegraNegocioException(
                        "Existem ordens de serviço com datas de execução inconsistentes"
                );
            }

            totalSegundos += Duration.between(inicio, fim).getSeconds();
        }

        int quantidade = ordensFinalizadas.size();
        BigDecimal mediaSegundos = quantidade == 0
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(totalSegundos)
                .divide(
                        BigDecimal.valueOf(quantidade),
                        2,
                        RoundingMode.HALF_UP
                );
        BigDecimal mediaMinutos = mediaSegundos.divide(
                BigDecimal.valueOf(60),
                2,
                RoundingMode.HALF_UP
        );

        return new TempoMedioExecucaoResponse(
                quantidade,
                mediaSegundos,
                mediaMinutos,
                LocalDateTime.now()
        );
    }

    @Transactional(readOnly = true)
    public OrdemServico buscarEntidadePorId(Long id) {
        return ordemServicoRepository.findOneById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Ordem de serviço não encontrada com o ID: " + id
                ));
    }

    private void validarVeiculoPertenceAoCliente(
            Cliente cliente,
            Veiculo veiculo
    ) {
        if (veiculo.getCliente() == null
                || veiculo.getCliente().getId() == null
                || !veiculo.getCliente().getId().equals(cliente.getId())) {

            throw new RegraNegocioException(
                    "O veículo informado não pertence ao cliente"
            );
        }
    }

    private String gerarNumeroOs() {
        String dataHora = LocalDateTime.now()
                .format(FORMATO_NUMERO_OS);

        String identificador = UUID.randomUUID()
                .toString()
                .substring(0, 6)
                .toUpperCase();

        return "OS-" + dataHora + "-" + identificador;
    }

    private String normalizarObservacoes(String observacoes) {
        if (observacoes == null || observacoes.isBlank()) {
            return null;
        }

        return observacoes.trim();
    }

    @Transactional
    public OrcamentoResponse calcularOrcamento(Long ordemServicoId) {
        OrdemServico ordemServico = buscarEntidadePorId(
                ordemServicoId
        );

        ordemServico.recalcularValorTotal();

        OrdemServico ordemAtualizada =
                ordemServicoRepository.save(ordemServico);

        return montarOrcamentoResponse(ordemAtualizada);
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse consultarOrcamento(
            Long ordemServicoId
    ) {
        OrdemServico ordemServico = buscarEntidadePorId(
                ordemServicoId
        );

        return montarOrcamentoResponse(ordemServico);
    }

    private OrcamentoResponse montarOrcamentoResponse(
            OrdemServico ordemServico
    ) {
        ordemServico.recalcularValorTotal();

        int quantidadeServicos =
                ordemServico.getItensServico() == null
                        ? 0
                        : ordemServico.getItensServico()
                        .stream()
                        .filter(item -> item != null)
                        .mapToInt(item ->
                                item.getQuantidade() == null
                                        ? 0
                                        : item.getQuantidade()
                        )
                        .sum();

        int quantidadePecas =
                ordemServico.getItensPeca() == null
                        ? 0
                        : ordemServico.getItensPeca()
                        .stream()
                        .filter(item -> item != null)
                        .mapToInt(item ->
                                item.getQuantidade() == null
                                        ? 0
                                        : item.getQuantidade()
                        )
                        .sum();

        return new OrcamentoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                quantidadeServicos,
                quantidadePecas,
                ordemServico.calcularTotalServicos(),
                ordemServico.calcularTotalPecas(),
                ordemServico.getValorTotal(),
                LocalDateTime.now()
        );
    }

    @Transactional
    public OrdemServicoResponse adicionarServico(
            Long ordemServicoId,
            AdicionarServicoOrdemRequest request
    ) {
        OrdemServico ordemServico =
                buscarEntidadePorId(ordemServicoId);

        Servico servico = servicoService.buscarEntidadePorId(
                request.servicoId()
        );

        validarServicoAtivo(servico);

        ItemServico itemExistente =
                ordemServico.buscarItemServicoPorServicoId(
                        servico.getId()
                );

        if (itemExistente != null) {
            ordemServico.aumentarQuantidadeServico(
                    itemExistente,
                    request.quantidade()
            );
        } else {
            ItemServico novoItem = ItemServico.builder()
                    .servico(servico)
                    .quantidade(request.quantidade())
                    .valorUnitario(obterValorServico(servico))
                    .build();

            ordemServico.adicionarServico(novoItem);
        }

        ordemServico.recalcularValorTotal();

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return OrdemServicoMapper.paraResponse(ordemSalva);
    }

    @Transactional
    public OrdemServicoResponse adicionarPeca(
            Long ordemServicoId,
            AdicionarPecaOrdemRequest request
    ) {
        OrdemServico ordemServico =
                buscarEntidadePorId(ordemServicoId);

        Peca peca = pecaService.buscarEntidadePorId(
                request.pecaId()
        );

        validarPecaAtiva(peca);

        ItemPeca itemExistente =
                ordemServico.buscarItemPecaPorPecaId(
                        peca.getId()
                );

        int quantidadeAtual = itemExistente == null
                ? 0
                : itemExistente.getQuantidade();

        int quantidadeFinal =
                quantidadeAtual + request.quantidade();

        validarEstoqueDisponivel(peca, quantidadeFinal);

        if (itemExistente != null) {
            ordemServico.aumentarQuantidadePeca(
                    itemExistente,
                    request.quantidade()
            );
        } else {
            ItemPeca novoItem = ItemPeca.builder()
                    .peca(peca)
                    .quantidade(request.quantidade())
                    .valorUnitario(obterValorPeca(peca))
                    .build();

            ordemServico.adicionarPeca(novoItem);
        }

        ordemServico.recalcularValorTotal();

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return OrdemServicoMapper.paraResponse(ordemSalva);
    }

    @Transactional
    public OrdemServicoResponse removerPeca(
            Long ordemServicoId,
            Long pecaId
    ) {
        OrdemServico ordemServico = buscarEntidadePorId(ordemServicoId);
        ItemPeca itemPeca = ordemServico.buscarItemPecaPorPecaId(pecaId);

        if (itemPeca == null) {
            throw new RecursoNaoEncontradoException(
                    "A peça informada não está vinculada à ordem de serviço"
            );
        }

        ordemServico.removerPeca(itemPeca);

        return OrdemServicoMapper.paraResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    private void validarServicoAtivo(Servico servico) {
        if (Boolean.FALSE.equals(servico.getAtivo())) {
            throw new RegraNegocioException(
                    "O serviço informado está inativo"
            );
        }
    }

    private void validarPecaAtiva(Peca peca) {
        if (Boolean.FALSE.equals(peca.getAtivo())) {
            throw new RegraNegocioException(
                    "A peça informada está inativa"
            );
        }
    }

    private BigDecimal obterValorServico(Servico servico) {
        if (servico.getValor() == null) {
            throw new RegraNegocioException(
                    "O serviço não possui valor cadastrado"
            );
        }

        return servico.getValor();
    }

    private BigDecimal obterValorPeca(Peca peca) {
        if (peca.getValor() == null) {
            throw new RegraNegocioException(
                    "A peça não possui valor cadastrado"
            );
        }

        return peca.getValor();
    }

    private void adicionarServicos(
            OrdemServico ordemServico,
            List<ItemServicoRequest> itens
    ) {
        List<ItemServicoRequest> itensSeguros =
                itens == null ? Collections.emptyList() : itens;

        for (ItemServicoRequest itemRequest : itensSeguros) {
            Servico servico =
                    servicoService.buscarEntidadePorId(
                            itemRequest.servicoId()
                    );

            validarServicoAtivo(servico);
            ItemServico itemExistente =
                    ordemServico.buscarItemServicoPorServicoId(servico.getId());

            if (itemExistente == null) {
                ItemServico itemServico = ItemServico.builder()
                        .servico(servico)
                        .quantidade(itemRequest.quantidade())
                        .valorUnitario(obterValorServico(servico))
                        .build();

                ordemServico.adicionarServico(itemServico);
            } else {
                ordemServico.aumentarQuantidadeServico(
                        itemExistente,
                        itemRequest.quantidade()
                );
            }
        }
    }

    private void adicionarPecas(
            OrdemServico ordemServico,
            List<ItemPecaRequest> itens
    ) {
        List<ItemPecaRequest> itensSeguros =
                itens == null ? Collections.emptyList() : itens;

        for (ItemPecaRequest itemRequest : itensSeguros) {
            Peca peca =
                    pecaService.buscarEntidadePorId(
                            itemRequest.pecaId()
                    );

            validarPecaAtiva(peca);
            ItemPeca itemExistente =
                    ordemServico.buscarItemPecaPorPecaId(peca.getId());
            int quantidadeAtual = itemExistente == null
                    ? 0
                    : itemExistente.getQuantidade();
            int quantidadeFinal = quantidadeAtual + itemRequest.quantidade();

            validarEstoqueDisponivel(peca, quantidadeFinal);

            if (itemExistente == null) {
                ItemPeca itemPeca = ItemPeca.builder()
                        .peca(peca)
                        .quantidade(itemRequest.quantidade())
                        .valorUnitario(obterValorPeca(peca))
                        .build();

                ordemServico.adicionarPeca(itemPeca);
            } else {
                ordemServico.aumentarQuantidadePeca(
                        itemExistente,
                        itemRequest.quantidade()
                );
            }
        }
    }

    private void validarEstoqueDisponivel(
            Peca peca,
            Integer quantidadeSolicitada
    ) {
        if (quantidadeSolicitada == null || quantidadeSolicitada <= 0) {
            throw new RegraNegocioException(
                    "A quantidade da peça deve ser maior que zero"
            );
        }

        if (peca.getQuantidadeEstoque() == null
                || peca.getQuantidadeEstoque() < quantidadeSolicitada) {

            throw new RegraNegocioException(
                    "Estoque insuficiente para a peça "
                            + peca.getNome()
                            + ". Disponível: "
                            + peca.getQuantidadeEstoque()
                            + ". Solicitado: "
                            + quantidadeSolicitada
            );
        }
    }

    @Transactional
    public AprovacaoOrcamentoResponse enviarParaAprovacao(
            Long ordemServicoId
    ) {
        OrdemServico ordemServico =
                buscarEntidadePorId(ordemServicoId);

        ordemServico.enviarParaAprovacao();

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return montarAprovacaoOrcamentoResponse(ordemSalva);
    }

    @Transactional
    public AprovacaoOrcamentoResponse aprovarOrcamento(
            Long ordemServicoId,
            AprovacaoOrcamentoRequest request
    ) {
        OrdemServico ordemServico =
                buscarEntidadePorId(ordemServicoId);

        String observacao =
                request == null
                        ? null
                        : request.observacao();

        ordemServico.aprovarOrcamentoAutomaticamente(observacao);

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return montarAprovacaoOrcamentoResponse(ordemSalva);
    }

    @Transactional
    public AprovacaoOrcamentoResponse recusarOrcamento(
            Long ordemServicoId,
            AprovacaoOrcamentoRequest request
    ) {
        OrdemServico ordemServico =
                buscarEntidadePorId(ordemServicoId);

        String observacao =
                request == null
                        ? null
                        : request.observacao();

        ordemServico.recusarOrcamentoAutomaticamente(observacao);

        OrdemServico ordemSalva =
                ordemServicoRepository.save(ordemServico);

        return montarAprovacaoOrcamentoResponse(ordemSalva);
    }

    @Transactional
    public OrdemServicoResponse finalizar(Long ordemServicoId) {
        OrdemServico ordemServico = buscarEntidadePorId(ordemServicoId);

        ordemServico.finalizarExecucao();

        return OrdemServicoMapper.paraResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    @Transactional
    public OrdemServicoResponse entregar(Long ordemServicoId) {
        OrdemServico ordemServico = buscarEntidadePorId(ordemServicoId);

        ordemServico.entregarVeiculo();

        return OrdemServicoMapper.paraResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    @Transactional
    public OrdemServicoResponse cancelar(Long ordemServicoId) {
        OrdemServico ordemServico = buscarEntidadePorId(ordemServicoId);

        ordemServico.cancelar();

        return OrdemServicoMapper.paraResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    private AprovacaoOrcamentoResponse
    montarAprovacaoOrcamentoResponse(
            OrdemServico ordemServico
    ) {
        return new AprovacaoOrcamentoResponse(
                ordemServico.getId(),
                ordemServico.getNumeroOs(),
                ordemServico.getStatus(),
                ordemServico.getStatusAprovacaoOrcamento(),
                ordemServico.getValorTotal(),
                ordemServico.getDataAprovacaoOrcamento(),
                ordemServico.getObservacaoAprovacaoOrcamento()
        );
    }
}
