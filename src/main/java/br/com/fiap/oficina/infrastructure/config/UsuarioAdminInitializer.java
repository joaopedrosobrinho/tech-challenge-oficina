package br.com.fiap.oficina.infrastructure.config;

import br.com.fiap.oficina.domain.usuario.PerfilUsuario;
import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class UsuarioAdminInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${oficina.security.admin.email}")
    private String email;

    @Value("${oficina.security.admin.password}")
    private String senha;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);

        if (usuarioRepository.existsByEmailIgnoreCase(emailNormalizado)) {
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .email(emailNormalizado)
                .senha(passwordEncoder.encode(senha))
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build());
    }
}
