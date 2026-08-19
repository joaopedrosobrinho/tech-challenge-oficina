package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.autenticacao.LoginRequest;
import br.com.fiap.oficina.application.dto.autenticacao.LoginResponse;
import br.com.fiap.oficina.application.exception.CredenciaisInvalidasException;
import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.infrastructure.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AutenticacaoService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public LoginResponse autenticar(LoginRequest request) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .filter(encontrado -> passwordEncoder.matches(
                        request.senha(), encontrado.getSenha()
                ))
                .orElseThrow(CredenciaisInvalidasException::new);

        JwtService.TokenGerado token = jwtService.gerarToken(usuario);

        return new LoginResponse(
                usuario.getEmail(),
                usuario.getPerfil(),
                "Bearer",
                token.valor(),
                token.expiraEm()
        );
    }
}
