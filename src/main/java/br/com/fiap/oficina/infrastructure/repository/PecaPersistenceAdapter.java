package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.peca.Peca;
import java.util.List;
import java.util.Optional;
import br.com.fiap.oficina.application.port.out.PecaStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PecaPersistenceAdapter implements PecaStore {
    private final PecaRepository repository;

    @Override
    public Optional<Peca> findByCodigoIgnoreCase(String codigo) {
        return repository.findByCodigoIgnoreCase(codigo);
    }

    @Override
    public List<Peca> findAllByAtivoTrueOrderByNomeAsc() {
        return repository.findAllByAtivoTrueOrderByNomeAsc();
    }

    @Override
    public List<Peca> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome) {
        return repository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome);
    }

    @Override
    public List<Peca> findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(Integer quantidade) {
        return repository.findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(quantidade);
    }

    @Override
    public boolean existsByCodigoIgnoreCase(String codigo) {
        return repository.existsByCodigoIgnoreCase(codigo);
    }

    @Override
    public boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id) {
        return repository.existsByCodigoIgnoreCaseAndIdNot(codigo, id);
    }

    @Override
    public <S extends Peca> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Peca> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Peca> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Peca entity) {
        repository.delete(entity);
    }
}
