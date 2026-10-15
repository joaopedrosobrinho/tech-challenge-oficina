package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
public interface ConsultaVeiculoPort {
    Veiculo buscarEntidadePorId(Long id);
}
