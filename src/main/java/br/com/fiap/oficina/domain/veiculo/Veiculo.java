package br.com.fiap.oficina.domain.veiculo;

import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Veiculo {

    @EqualsAndHashCode.Include
    private Long id;

    private String placa;

    private String marca;

    private String modelo;

    private Integer ano;

    private Cliente cliente;

    @Builder.Default

    private List<OrdemServico> ordensServico = new ArrayList<>();

    public void adicionarOrdemServico(OrdemServico ordemServico) {
        if (ordemServico == null) {
            throw new IllegalArgumentException(
                    "A ordem de serviço não pode ser nula"
            );
        }

        ordensServico.add(ordemServico);
        ordemServico.setVeiculo(this);
    }
}
