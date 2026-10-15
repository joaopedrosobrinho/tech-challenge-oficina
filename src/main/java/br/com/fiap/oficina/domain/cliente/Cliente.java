package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {

    @EqualsAndHashCode.Include
    private Long id;

    private String nome;

    private String cpfCnpj;

    private String telefone;

    private String email;

    @Builder.Default

    private List<Veiculo> veiculos = new ArrayList<>();

    @Builder.Default

    private List<OrdemServico> ordensServico = new ArrayList<>();

    public void adicionarVeiculo(Veiculo veiculo) {
        if (veiculo == null) {
            throw new IllegalArgumentException("O veículo não pode ser nulo");
        }

        veiculos.add(veiculo);
        veiculo.setCliente(this);
    }

    public void removerVeiculo(Veiculo veiculo) {
        if (veiculo == null) {
            return;
        }

        veiculos.remove(veiculo);
        veiculo.setCliente(null);
    }

    public void adicionarOrdemServico(OrdemServico ordemServico) {
        if (ordemServico == null) {
            throw new IllegalArgumentException(
                    "A ordem de serviço não pode ser nula"
            );
        }

        ordensServico.add(ordemServico);
        ordemServico.setCliente(this);
    }
}
