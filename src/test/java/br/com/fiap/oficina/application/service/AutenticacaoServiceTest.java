package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.autenticacao.LoginRequest;
import br.com.fiap.oficina.application.exception.CredenciaisInvalidasException;
import br.com.fiap.oficina.domain.usuario.PerfilUsuario;
import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AutenticacaoServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AutenticacaoService service;

    @BeforeEach
    void configurar() {
        service = new AutenticacaoService(
                usuarioRepository, passwordEncoder, jwtService
        );
    }

    @Test
    void deveAutenticarUsuarioAtivoComSenhaCorreta() {
        Usuario usuario = Usuario.builder()
                .email("admin@oficina.com")
                .senha("hash")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@oficina.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha", "hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn(
                new JwtService.TokenGerado("token-assinado", Instant.parse("2026-08-18T22:00:00Z"))
        );

        var response = service.autenticar(
                new LoginRequest(" ADMIN@OFICINA.COM ", "senha")
        );

        assertThat(response.email()).isEqualTo("admin@oficina.com");
        assertThat(response.perfil()).isEqualTo(PerfilUsuario.ADMIN);
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.token()).isEqualTo("token-assinado");
        verify(passwordEncoder).matches("senha", "hash");
    }

    @Test
    void deveRejeitarSenhaIncorretaSemInformarQualCredencialFalhou() {
        Usuario usuario = Usuario.builder()
                .email("admin@oficina.com")
                .senha("hash")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@oficina.com"))
                .thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.autenticar(
                new LoginRequest("admin@oficina.com", "errada")
        )).isInstanceOf(CredenciaisInvalidasException.class)
                .hasMessage("E-mail ou senha inválidos");
    }
}
