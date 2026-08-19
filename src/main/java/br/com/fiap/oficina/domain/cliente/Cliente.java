package br.com.fiap.oficina.domain.cliente;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cliente_cpf_cnpj",
                        columnNames = "cpf_cnpj"
                ),
                @UniqueConstraint(
                        name = "uk_cliente_email",
                        columnNames = "email"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(name = "cpf_cnpj", nullable = false, length = 14)
    private String cpfCnpj;

    @Column(nullable = false, length = 20)
    private String telefone;

    @Column(nullable = false, length = 150)
    private String email;

    @Builder.Default
    @OneToMany(
            mappedBy = "cliente",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Veiculo> veiculos = new ArrayList<>();

    @Builder.Default
    @OneToMany(mappedBy = "cliente")
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