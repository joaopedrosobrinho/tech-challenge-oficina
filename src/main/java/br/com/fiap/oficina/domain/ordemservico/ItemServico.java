package br.com.fiap.oficina.domain.ordemservico;

import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.common.RegraNegocioException;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemServico {

    private Long id;

    private OrdemServico ordemServico;

    private Servico servico;

    private Integer quantidade;

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
                    "A quantidade adicional do serviço deve ser maior que zero"
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

        if (!(outro instanceof ItemServico itemServico)) {
            return false;
        }

        return id != null && id.equals(itemServico.id);
    }

    @Override
    public int hashCode() {
        return ItemServico.class.hashCode();
    }
}
