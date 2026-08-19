package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.domain.veiculo.Veiculo;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(
        name = "ordens_servico",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ordem_servico_numero",
                        columnNames = "numero_os"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(
            name = "numero_os",
            nullable = false,
            length = 30,
            unique = true
    )
    private String numeroOs;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_os_cliente")
    )
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "veiculo_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_os_veiculo")
    )
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status_aprovacao_orcamento",
            length = 30
    )
    @Builder.Default
    private StatusAprovacaoOrcamento statusAprovacaoOrcamento =
            StatusAprovacaoOrcamento.PENDENTE;

    @Column(name = "data_aprovacao_orcamento")
    private LocalDateTime dataAprovacaoOrcamento;

    @Column(
            name = "observacao_aprovacao_orcamento",
            length = 500
    )
    private String observacaoAprovacaoOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StatusOrdemServico status;

    @Builder.Default
    @Column(
            name = "valor_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Column(name = "observacoes", length = 1000)
    private String observacoes;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "data_envio_orcamento")
    private LocalDateTime dataEnvioOrcamento;

    @Column(name = "data_aprovacao")
    private LocalDateTime dataAprovacao;

    @Column(name = "data_inicio_execucao")
    private LocalDateTime dataInicioExecucao;

    @Column(name = "data_finalizacao")
    private LocalDateTime dataFinalizacao;

    @Column(name = "data_entrega")
    private LocalDateTime dataEntrega;

    @Column(name = "data_cancelamento")
    private LocalDateTime dataCancelamento;

    @Builder.Default
    @Column(name = "estoque_baixado", nullable = false)
    private Boolean estoqueBaixado = false;

    @Column(name = "data_baixa_estoque")
    private LocalDateTime dataBaixaEstoque;

    @Builder.Default
    @Column(name = "estoque_devolvido", nullable = false)
    private Boolean estoqueDevolvido = false;

    @Column(name = "data_devolucao_estoque")
    private LocalDateTime dataDevolucaoEstoque;

    @Builder.Default
    @OneToMany(
            mappedBy = "ordemServico",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ItemServico> itensServico = new LinkedHashSet<>();

    @Builder.Default
    @OneToMany(
            mappedBy = "ordemServico",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<ItemPeca> itensPeca = new LinkedHashSet<>();

    @PrePersist
    public void antesDeSalvar() {
        if (dataCriacao == null) {
            dataCriacao = LocalDateTime.now();
        }

        if (status == null) {
            status = StatusOrdemServico.RECEBIDA;
        }

        if (statusAprovacaoOrcamento == null) {
            statusAprovacaoOrcamento =
                    StatusAprovacaoOrcamento.PENDENTE;
        }

        garantirColecoesInicializadas();
        validarDadosObrigatorios();
        validarVeiculoPertenceAoCliente();
        recalcularValorTotal();
    }

    @PreUpdate
    public void antesDeAtualizar() {
        garantirColecoesInicializadas();
        validarDadosObrigatorios();
        validarVeiculoPertenceAoCliente();
        recalcularValorTotal();
    }

    public void definirClienteEVeiculo(
            Cliente cliente,
            Veiculo veiculo
    ) {
        if (cliente == null) {
            throw new RegraNegocioException(
                    "O cliente é obrigatório para criar a ordem de serviço"
            );
        }

        if (veiculo == null) {
            throw new RegraNegocioException(
                    "O veículo é obrigatório para criar a ordem de serviço"
            );
        }

        this.cliente = cliente;
        this.veiculo = veiculo;

        validarVeiculoPertenceAoCliente();
    }

    public void removerServico(ItemServico itemServico) {
        validarAlteracaoDeItens();

        if (itemServico == null) {
            return;
        }

        garantirColecoesInicializadas();

        if (itensServico.remove(itemServico)) {
            itemServico.setOrdemServico(null);
            recalcularValorTotal();
        }
    }

    public void removerPeca(ItemPeca itemPeca) {
        validarAlteracaoDeItens();

        if (itemPeca == null) {
            return;
        }

        garantirColecoesInicializadas();

        if (itensPeca.remove(itemPeca)) {
            itemPeca.setOrdemServico(null);
            recalcularValorTotal();
        }
    }

    public BigDecimal calcularTotalServicos() {
        garantirColecoesInicializadas();

        return itensServico.stream()
                .filter(Objects::nonNull)
                .map(ItemServico::calcularSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalPecas() {
        garantirColecoesInicializadas();

        return itensPeca.stream()
                .filter(Objects::nonNull)
                .map(ItemPeca::calcularSubtotal)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                )
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void recalcularValorTotal() {
        BigDecimal totalServicos = calcularTotalServicos();
        BigDecimal totalPecas = calcularTotalPecas();

        valorTotal = totalServicos
                .add(totalPecas)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean podeAlterarOrcamento() {
        return status == StatusOrdemServico.RECEBIDA
                || status == StatusOrdemServico.EM_DIAGNOSTICO;
    }

    public boolean estaFinalizada() {
        return status == StatusOrdemServico.FINALIZADA;
    }

    public boolean estaEntregue() {
        return status == StatusOrdemServico.ENTREGUE;
    }

    public boolean estaEmExecucao() {
        return status == StatusOrdemServico.EM_EXECUCAO;
    }

    private void validarDadosObrigatorios() {
        if (numeroOs == null || numeroOs.isBlank()) {
            throw new RegraNegocioException(
                    "O número da ordem de serviço é obrigatório"
            );
        }

        if (cliente == null) {
            throw new RegraNegocioException(
                    "O cliente da ordem de serviço é obrigatório"
            );
        }

        if (veiculo == null) {
            throw new RegraNegocioException(
                    "O veículo da ordem de serviço é obrigatório"
            );
        }
    }

    private void validarVeiculoPertenceAoCliente() {
        if (cliente == null || veiculo == null) {
            return;
        }

        Cliente proprietario = veiculo.getCliente();

        if (proprietario == null) {
            throw new RegraNegocioException(
                    "O veículo informado não possui cliente vinculado"
            );
        }

        if (cliente.getId() != null && proprietario.getId() != null) {
            if (!cliente.getId().equals(proprietario.getId())) {
                throw new RegraNegocioException(
                        "O veículo informado não pertence ao cliente"
                );
            }

            return;
        }

        if (cliente != proprietario) {
            throw new RegraNegocioException(
                    "O veículo informado não pertence ao cliente"
            );
        }
    }

    private void validarOrcamento() {
        recalcularValorTotal();

        if (itensServico.isEmpty()) {
            throw new RegraNegocioException(
                    "A ordem de serviço deve possuir pelo menos um serviço"
            );
        }

        if (valorTotal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraNegocioException(
                    "O valor total do orçamento deve ser maior que zero"
            );
        }
    }

    private void garantirColecoesInicializadas() {
        if (itensServico == null) {
            itensServico = new LinkedHashSet<>();
        }

        if (itensPeca == null) {
            itensPeca = new LinkedHashSet<>();
        }
    }

    public void adicionarServico(ItemServico itemServico) {
        validarAlteracaoDeItens();

        if (itemServico == null) {
            throw new RegraNegocioException(
                    "O item de serviço não pode ser nulo"
            );
        }

        if (itemServico.getServico() == null) {
            throw new RegraNegocioException(
                    "O serviço do item é obrigatório"
            );
        }

        if (itemServico.getQuantidade() == null
                || itemServico.getQuantidade() <= 0) {

            throw new RegraNegocioException(
                    "A quantidade do serviço deve ser maior que zero"
            );
        }

        garantirColecoesInicializadas();

        itemServico.setOrdemServico(this);
        itensServico.add(itemServico);

        alterarParaDiagnosticoSeNecessario();
        recalcularValorTotal();
    }

    public void adicionarPeca(ItemPeca itemPeca) {
        validarAlteracaoDeItens();

        if (itemPeca == null) {
            throw new RegraNegocioException(
                    "O item de peça não pode ser nulo"
            );
        }

        if (itemPeca.getPeca() == null) {
            throw new RegraNegocioException(
                    "A peça do item é obrigatória"
            );
        }

        if (itemPeca.getQuantidade() == null
                || itemPeca.getQuantidade() <= 0) {

            throw new RegraNegocioException(
                    "A quantidade da peça deve ser maior que zero"
            );
        }

        garantirColecoesInicializadas();

        itemPeca.setOrdemServico(this);
        itensPeca.add(itemPeca);

        alterarParaDiagnosticoSeNecessario();
        recalcularValorTotal();
    }

    public void aumentarQuantidadeServico(
            ItemServico itemServico,
            Integer quantidadeAdicional
    ) {
        validarAlteracaoDeItens();
        garantirColecoesInicializadas();

        if (itemServico == null || !itensServico.contains(itemServico)) {
            throw new RegraNegocioException(
                    "O item de serviço não pertence à ordem de serviço"
            );
        }

        itemServico.aumentarQuantidade(quantidadeAdicional);
        alterarParaDiagnosticoSeNecessario();
        recalcularValorTotal();
    }

    public void aumentarQuantidadePeca(
            ItemPeca itemPeca,
            Integer quantidadeAdicional
    ) {
        validarAlteracaoDeItens();
        garantirColecoesInicializadas();

        if (itemPeca == null || !itensPeca.contains(itemPeca)) {
            throw new RegraNegocioException(
                    "O item de peça não pertence à ordem de serviço"
            );
        }

        itemPeca.aumentarQuantidade(quantidadeAdicional);
        alterarParaDiagnosticoSeNecessario();
        recalcularValorTotal();
    }

    private void validarAlteracaoDeItens() {
        if (status == StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException(
                    "Não é possível alterar os itens enquanto o orçamento aguarda aprovação."
            );
        }

        if (status == StatusOrdemServico.EM_EXECUCAO) {
            throw new RegraNegocioException(
                    "Não é possível alterar os itens durante a execução da ordem de serviço."
            );
        }

        if (status == StatusOrdemServico.FINALIZADA
                || status == StatusOrdemServico.ENTREGUE
                || status == StatusOrdemServico.CANCELADA) {

            throw new RegraNegocioException(
                    "Não é possível alterar os itens no status atual da ordem de serviço."
            );
        }
    }

    public ItemServico buscarItemServicoPorServicoId(
            Long servicoId
    ) {
        garantirColecoesInicializadas();

        return itensServico.stream()
                .filter(item -> item.getServico() != null)
                .filter(item -> item.getServico().getId() != null)
                .filter(item ->
                        item.getServico().getId().equals(servicoId)
                )
                .findFirst()
                .orElse(null);
    }

    public ItemPeca buscarItemPecaPorPecaId(Long pecaId) {
        garantirColecoesInicializadas();

        return itensPeca.stream()
                .filter(item -> item.getPeca() != null)
                .filter(item -> item.getPeca().getId() != null)
                .filter(item ->
                        item.getPeca().getId().equals(pecaId)
                )
                .findFirst()
                .orElse(null);
    }

    private void validarOrcamentoAguardandoAprovacao() {

        if (status != StatusOrdemServico.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException(
                    "A ordem de serviço não está aguardando aprovação"
            );
        }

        if (statusAprovacaoOrcamento
                == StatusAprovacaoOrcamento.APROVADO) {

            throw new RegraNegocioException(
                    "O orçamento já foi aprovado"
            );
        }
    }

    private String normalizarObservacaoAprovacao(
            String observacao
    ) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }

        return observacao.trim();
    }

    public void iniciarDiagnostico() {
        validarStatusAtual(
                StatusOrdemServico.RECEBIDA,
                "Somente uma ordem recebida pode iniciar o diagnóstico."
        );

        this.status = StatusOrdemServico.EM_DIAGNOSTICO;
    }

    public void enviarParaAprovacao() {
        if (this.status != StatusOrdemServico.RECEBIDA
                && this.status != StatusOrdemServico.EM_DIAGNOSTICO) {

            throw new RegraNegocioException(
                    "O orçamento somente pode ser enviado para aprovação " +
                            "quando a ordem estiver recebida ou em diagnóstico."
            );
        }

        if (this.itensServico == null || this.itensServico.isEmpty()) {
            throw new RegraNegocioException(
                    "Não é possível enviar um orçamento sem serviços."
            );
        }

        recalcularValorTotal();

        if (this.valorTotal == null
                || this.valorTotal.compareTo(BigDecimal.ZERO) <= 0) {

            throw new RegraNegocioException(
                    "O valor total do orçamento deve ser maior que zero."
            );
        }

        this.status = StatusOrdemServico.AGUARDANDO_APROVACAO;
        this.statusAprovacaoOrcamento =
                StatusAprovacaoOrcamento.PENDENTE;
        this.dataEnvioOrcamento = LocalDateTime.now();
        this.dataAprovacaoOrcamento = null;
        this.observacaoAprovacaoOrcamento = null;
    }

    public void aprovarOrcamentoAutomaticamente(String observacao) {
        validarStatusAtual(
                StatusOrdemServico.AGUARDANDO_APROVACAO,
                "Somente uma ordem aguardando aprovação pode ter o orçamento aprovado."
        );

        if (this.statusAprovacaoOrcamento
                != StatusAprovacaoOrcamento.PENDENTE) {

            throw new RegraNegocioException(
                    "O orçamento não está pendente de aprovação."
            );
        }

        baixarEstoqueDasPecas();

        LocalDateTime agora = LocalDateTime.now();

        this.statusAprovacaoOrcamento =
                StatusAprovacaoOrcamento.APROVADO;
        this.dataAprovacaoOrcamento = agora;
        this.dataAprovacao = agora;
        this.observacaoAprovacaoOrcamento =
                normalizarObservacaoAprovacao(observacao);

        this.status = StatusOrdemServico.EM_EXECUCAO;
        this.dataInicioExecucao = agora;
    }

    private void baixarEstoqueDasPecas() {
        if (Boolean.TRUE.equals(estoqueBaixado)) {
            throw new RegraNegocioException(
                    "O estoque desta ordem de serviço já foi baixado."
            );
        }

        Map<Peca, Integer> quantidades =
                consolidarQuantidadesPecas();

        for (Map.Entry<Peca, Integer> entrada
                : quantidades.entrySet()) {
            if (!entrada.getKey().possuiEstoque(entrada.getValue())) {
                throw new RegraNegocioException(
                        "Estoque insuficiente para a peça "
                                + entrada.getKey().getNome()
                                + ". Disponível: "
                                + entrada.getKey().getQuantidadeEstoque()
                                + ". Necessário: "
                                + entrada.getValue()
                );
            }
        }

        for (Map.Entry<Peca, Integer> entrada
                : quantidades.entrySet()) {
            entrada.getKey().baixarEstoque(entrada.getValue());
        }

        estoqueBaixado = true;
        dataBaixaEstoque = LocalDateTime.now();
    }

    public void recusarOrcamentoAutomaticamente(String observacao) {
        validarStatusAtual(
                StatusOrdemServico.AGUARDANDO_APROVACAO,
                "Somente uma ordem aguardando aprovação pode ter o orçamento recusado."
        );

        if (this.statusAprovacaoOrcamento
                != StatusAprovacaoOrcamento.PENDENTE) {

            throw new RegraNegocioException(
                    "O orçamento não está pendente de aprovação."
            );
        }

        String observacaoNormalizada =
                normalizarObservacaoAprovacao(observacao);

        if (observacaoNormalizada == null) {
            throw new RegraNegocioException(
                    "A observação é obrigatória para recusar o orçamento."
            );
        }

        this.statusAprovacaoOrcamento =
                StatusAprovacaoOrcamento.RECUSADO;
        this.dataAprovacaoOrcamento = LocalDateTime.now();
        this.observacaoAprovacaoOrcamento =
                observacaoNormalizada;

        this.status = StatusOrdemServico.EM_DIAGNOSTICO;
    }

    public void finalizarExecucao() {
        validarStatusAtual(
                StatusOrdemServico.EM_EXECUCAO,
                "Somente uma ordem em execução pode ser finalizada."
        );

        this.status = StatusOrdemServico.FINALIZADA;
        this.dataFinalizacao = LocalDateTime.now();
    }

    public void entregarVeiculo() {
        validarStatusAtual(
                StatusOrdemServico.FINALIZADA,
                "Somente uma ordem finalizada pode ser entregue."
        );

        this.status = StatusOrdemServico.ENTREGUE;
        this.dataEntrega = LocalDateTime.now();
    }

    public void cancelar() {
        if (this.status == StatusOrdemServico.FINALIZADA
                || this.status == StatusOrdemServico.ENTREGUE) {
            throw new RegraNegocioException(
                    "Uma ordem finalizada ou entregue não pode ser cancelada."
            );
        }

        if (this.status == StatusOrdemServico.CANCELADA) {
            throw new RegraNegocioException(
                    "A ordem de serviço já está cancelada."
            );
        }

        devolverEstoqueSeNecessario();

        this.status = StatusOrdemServico.CANCELADA;
        this.dataCancelamento = LocalDateTime.now();
    }

    private void devolverEstoqueSeNecessario() {
        if (!Boolean.TRUE.equals(estoqueBaixado)) {
            return;
        }

        if (Boolean.TRUE.equals(estoqueDevolvido)) {
            throw new RegraNegocioException(
                    "O estoque desta ordem de serviço já foi devolvido."
            );
        }

        Map<Peca, Integer> quantidades =
                consolidarQuantidadesPecas();

        for (Map.Entry<Peca, Integer> entrada
                : quantidades.entrySet()) {
            entrada.getKey().devolverEstoque(entrada.getValue());
        }

        estoqueDevolvido = true;
        dataDevolucaoEstoque = LocalDateTime.now();
    }

    private Map<Peca, Integer> consolidarQuantidadesPecas() {
        garantirColecoesInicializadas();

        Map<Peca, Integer> quantidades =
                new IdentityHashMap<>();

        for (ItemPeca item : itensPeca) {
            if (item == null || item.getPeca() == null
                    || item.getQuantidade() == null
                    || item.getQuantidade() <= 0) {
                throw new RegraNegocioException(
                        "Existe um item de peça inválido na ordem de serviço."
                );
            }

            quantidades.merge(
                    item.getPeca(),
                    item.getQuantidade(),
                    Integer::sum
            );
        }

        return quantidades;
    }

    private void validarStatusAtual(
            StatusOrdemServico statusEsperado,
            String mensagemErro
    ) {
        if (this.status != statusEsperado) {
            throw new RegraNegocioException(mensagemErro);
        }
    }

    private void alterarParaDiagnosticoSeNecessario() {
        if (this.status == StatusOrdemServico.RECEBIDA) {
            this.status = StatusOrdemServico.EM_DIAGNOSTICO;
        }
    }
}
