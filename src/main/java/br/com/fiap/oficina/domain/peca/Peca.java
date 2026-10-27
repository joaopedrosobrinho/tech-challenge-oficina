package br.com.fiap.oficina.domain.peca;

import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Peca {

    @EqualsAndHashCode.Include
    private Long id;

    private Long versao;

    private String codigo;

    private String nome;

    private String descricao;

    private BigDecimal valor;

    private Integer quantidadeEstoque;

    @Builder.Default

    private Boolean ativo = true;

    @Builder.Default

    private List<ItemPeca> itensPeca = new ArrayList<>();

    public boolean possuiEstoque(Integer quantidadeSolicitada) {
        return quantidadeSolicitada != null
                && quantidadeSolicitada > 0
                && quantidadeEstoque != null
                && quantidadeEstoque >= quantidadeSolicitada;
    }

    public void baixarEstoque(Integer quantidade) {
        if (!possuiEstoque(quantidade)) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente para a peça: " + nome
            );
        }

        quantidadeEstoque -= quantidade;
    }

    public void devolverEstoque(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException(
                    "A quantidade devolvida deve ser maior que zero"
            );
        }

        if (quantidadeEstoque == null) {
            quantidadeEstoque = 0;
        }

        quantidadeEstoque += quantidade;
    }
}
