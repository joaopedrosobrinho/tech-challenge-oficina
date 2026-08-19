package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.veiculo.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findByPlacaIgnoreCase(String placa);

    List<Veiculo> findAllByClienteId(Long clienteId);

    boolean existsByPlacaIgnoreCase(String placa);

    boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id);
}