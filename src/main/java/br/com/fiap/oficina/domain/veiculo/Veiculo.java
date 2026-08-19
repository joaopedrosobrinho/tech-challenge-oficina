package br.com.fiap.oficina.domain.veiculo;

import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "veiculos",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_veiculo_placa",
                        columnNames = "placa"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 7)
    private String placa;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column(nullable = false)
    private Integer ano;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_veiculo_cliente")
    )
    private Cliente cliente;

    @Builder.Default
    @OneToMany(mappedBy = "veiculo")
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