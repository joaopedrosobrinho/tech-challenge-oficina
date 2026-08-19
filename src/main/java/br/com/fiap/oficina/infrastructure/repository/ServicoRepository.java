package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.servico.Servico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicoRepository extends JpaRepository<Servico, Long> {

    List<Servico> findAllByAtivoTrueOrderByNomeAsc();

    List<Servico> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);

    boolean existsByNomeIgnoreCase(String nome);

    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
}