package br.com.fiap.oficina.domain.peca;

import br.com.fiap.oficina.domain.ordemservico.ItemPeca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "pecas",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_peca_codigo",
                        columnNames = "codigo"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Version
    private Long versao;

    @Column(nullable = false, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "quantidade_estoque", nullable = false)
    private Integer quantidadeEstoque;

    @Builder.Default
    @Column(nullable = false)
    private Boolean ativo = true;

    @Builder.Default
    @OneToMany(mappedBy = "peca")
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
