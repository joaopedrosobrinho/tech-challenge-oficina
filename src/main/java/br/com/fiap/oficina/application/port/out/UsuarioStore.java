package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.usuario.Usuario;
import java.util.Optional;
import java.util.List;

public interface UsuarioStore {
    Optional<Usuario> findByEmailIgnoreCaseAndAtivoTrue(String email);
    boolean existsByEmailIgnoreCase(String email);
    <S extends Usuario> S save(S entity);
    Optional<Usuario> findById(Long id);
    List<Usuario> findAll();
    void delete(Usuario entity);
}
