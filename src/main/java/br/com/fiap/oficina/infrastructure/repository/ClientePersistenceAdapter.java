package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.cliente.Cliente;
import java.util.Optional;
import java.util.List;
import br.com.fiap.oficina.application.port.out.ClienteStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientePersistenceAdapter implements ClienteStore {
    private final ClienteRepository repository;

    @Override
    public Optional<Cliente> findByCpfCnpj(String cpfCnpj) {
        return repository.findByCpfCnpj(cpfCnpj);
    }

    @Override
    public Optional<Cliente> findByEmailIgnoreCase(String email) {
        return repository.findByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByCpfCnpj(String cpfCnpj) {
        return repository.existsByCpfCnpj(cpfCnpj);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id) {
        return repository.existsByCpfCnpjAndIdNot(cpfCnpj, id);
    }

    @Override
    public boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id) {
        return repository.existsByEmailIgnoreCaseAndIdNot(email, id);
    }

    @Override
    public <S extends Cliente> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Cliente> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Cliente> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Cliente entity) {
        repository.delete(entity);
    }
}
