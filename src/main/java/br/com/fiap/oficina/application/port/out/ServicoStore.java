package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.servico.Servico;
import java.util.List;
import java.util.Optional;

public interface ServicoStore {
    List<Servico> findAllByAtivoTrueOrderByNomeAsc();
    List<Servico> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
    boolean existsByNomeIgnoreCase(String nome);
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    <S extends Servico> S save(S entity);
    Optional<Servico> findById(Long id);
    List<Servico> findAll();
    void delete(Servico entity);
}
