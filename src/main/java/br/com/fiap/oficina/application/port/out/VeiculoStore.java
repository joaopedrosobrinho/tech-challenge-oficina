package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.veiculo.Veiculo;
import java.util.List;
import java.util.Optional;

public interface VeiculoStore {
    Optional<Veiculo> findByPlacaIgnoreCase(String placa);
    List<Veiculo> findAllByClienteId(Long clienteId);
    boolean existsByPlacaIgnoreCase(String placa);
    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);
    <S extends Veiculo> S save(S entity);
    Optional<Veiculo> findById(Long id);
    List<Veiculo> findAll();
    void delete(Veiculo entity);
}
