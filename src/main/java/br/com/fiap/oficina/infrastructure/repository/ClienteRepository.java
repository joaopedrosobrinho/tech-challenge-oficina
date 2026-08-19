package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.cliente.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByCpfCnpj(String cpfCnpj);

    Optional<Cliente> findByEmailIgnoreCase(String email);

    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}