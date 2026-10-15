package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.veiculo.Veiculo;
import java.util.List;
import java.util.Optional;
import br.com.fiap.oficina.application.port.out.VeiculoStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeiculoPersistenceAdapter implements VeiculoStore {
    private final VeiculoRepository repository;

    @Override
    public Optional<Veiculo> findByPlacaIgnoreCase(String placa) {
        return repository.findByPlacaIgnoreCase(placa);
    }

    @Override
    public List<Veiculo> findAllByClienteId(Long clienteId) {
        return repository.findAllByClienteId(clienteId);
    }

    @Override
    public boolean existsByPlacaIgnoreCase(String placa) {
        return repository.existsByPlacaIgnoreCase(placa);
    }

    @Override
    public boolean existsByPlacaIgnoreCaseAndIdNot(String placa, Long id) {
        return repository.existsByPlacaIgnoreCaseAndIdNot(placa, id);
    }

    @Override
    public <S extends Veiculo> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Veiculo> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Veiculo> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Veiculo entity) {
        repository.delete(entity);
    }
}
