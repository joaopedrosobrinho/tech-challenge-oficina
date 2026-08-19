package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.usuario.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCaseAndAtivoTrue(String email);

    boolean existsByEmailIgnoreCase(String email);
}
