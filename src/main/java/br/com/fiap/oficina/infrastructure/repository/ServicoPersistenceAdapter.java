package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.servico.Servico;
import java.util.List;
import java.util.Optional;
import br.com.fiap.oficina.application.port.out.ServicoStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServicoPersistenceAdapter implements ServicoStore {
    private final ServicoRepository repository;

    @Override
    public List<Servico> findAllByAtivoTrueOrderByNomeAsc() {
        return repository.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Override
    public List<Servico> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome) {
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    @Override
    public boolean existsByNomeIgnoreCase(String nome) {
        return repository.existsByNomeIgnoreCase(nome);
    }

    @Override
    public boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id) {
        return repository.existsByNomeIgnoreCaseAndIdNot(nome, id);
    }

    @Override
    public <S extends Servico> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Servico> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Servico> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Servico entity) {
        repository.delete(entity);
    }
}
