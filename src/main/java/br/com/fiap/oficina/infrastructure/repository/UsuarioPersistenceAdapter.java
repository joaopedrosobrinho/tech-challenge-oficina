package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.usuario.Usuario;
import java.util.Optional;
import java.util.List;
import br.com.fiap.oficina.application.port.out.UsuarioStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioPersistenceAdapter implements UsuarioStore {
    private final UsuarioRepository repository;

    @Override
    public Optional<Usuario> findByEmailIgnoreCaseAndAtivoTrue(String email) {
        return repository.findByEmailIgnoreCaseAndAtivoTrue(email);
    }

    @Override
    public boolean existsByEmailIgnoreCase(String email) {
        return repository.existsByEmailIgnoreCase(email);
    }

    @Override
    public <S extends Usuario> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<Usuario> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<Usuario> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(Usuario entity) {
        repository.delete(entity);
    }
}
