package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.peca.Peca;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import br.com.fiap.oficina.application.exception.RegraNegocioException;

@Entity
@Table(name = "itens_peca")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ordem_servico_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_peca_os")
    )
    private OrdemServico ordemServico;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "peca_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_item_peca_peca")
    )
    private Peca peca;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(
            name = "valor_unitario",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorUnitario;

    public BigDecimal calcularSubtotal() {
        if (valorUnitario == null
                || quantidade == null
                || quantidade <= 0) {

            return BigDecimal.ZERO.setScale(
                    2,
                    RoundingMode.HALF_UP
            );
        }

        return valorUnitario
                .multiply(BigDecimal.valueOf(quantidade))
                .setScale(2, RoundingMode.HALF_UP);
    }

    public void aumentarQuantidade(Integer quantidadeAdicional) {
        if (quantidadeAdicional == null || quantidadeAdicional <= 0) {
            throw new RegraNegocioException(
                    "A quantidade adicional da peça deve ser maior que zero"
            );
        }

        if (quantidade == null) {
            quantidade = 0;
        }

        quantidade += quantidadeAdicional;
    }

    @Override
    public boolean equals(Object outro) {
        if (this == outro) {
            return true;
        }

        if (!(outro instanceof ItemPeca itemPeca)) {
            return false;
        }

        return id != null && id.equals(itemPeca.id);
    }

    @Override
    public int hashCode() {
        return ItemPeca.class.hashCode();
    }
}
