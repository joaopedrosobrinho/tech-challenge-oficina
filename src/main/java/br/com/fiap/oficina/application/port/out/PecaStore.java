package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.peca.Peca;
import java.util.List;
import java.util.Optional;

public interface PecaStore {
    Optional<Peca> findByCodigoIgnoreCase(String codigo);
    List<Peca> findAllByAtivoTrueOrderByNomeAsc();
    List<Peca> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
    List<Peca> findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(Integer quantidade);
    boolean existsByCodigoIgnoreCase(String codigo);
    boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);
    <S extends Peca> S save(S entity);
    Optional<Peca> findById(Long id);
    List<Peca> findAll();
    void delete(Peca entity);
}
