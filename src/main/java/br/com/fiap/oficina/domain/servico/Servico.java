package br.com.fiap.oficina.domain.servico;

import br.com.fiap.oficina.domain.ordemservico.ItemServico;
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
public class Servico {

    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    private String descricao;

    private BigDecimal valor;

    private Integer tempoEstimadoMinutos;

    @Builder.Default

    private Boolean ativo = true;

    @Builder.Default

    private List<ItemServico> itensServico = new ArrayList<>();
}
