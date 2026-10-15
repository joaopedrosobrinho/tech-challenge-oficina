package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.cliente.Cliente;
import java.util.Optional;
import java.util.List;

public interface ClienteStore {
    Optional<Cliente> findByCpfCnpj(String cpfCnpj);
    Optional<Cliente> findByEmailIgnoreCase(String email);
    boolean existsByCpfCnpj(String cpfCnpj);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    <S extends Cliente> S save(S entity);
    Optional<Cliente> findById(Long id);
    List<Cliente> findAll();
    void delete(Cliente entity);
}
