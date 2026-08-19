package br.com.fiap.oficina.infrastructure.security;

import br.com.fiap.oficina.domain.usuario.PerfilUsuario;
import br.com.fiap.oficina.domain.usuario.Usuario;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET =
            "b2ZpY2luYS10ZXN0ZS1qd3Qtc2VjcmV0LXNlZ3Vyby0yMDI2LTAx";
    private static final Instant AGORA = Instant.parse("2026-08-18T20:00:00Z");

    @Test
    void deveGerarTokenAssinadoComClaimsEValidade() {
        JwtService service = serviceNoInstante(AGORA);
        Usuario usuario = usuarioAdmin();

        JwtService.TokenGerado token = service.gerarToken(usuario);

        assertThat(token.valor()).isNotBlank();
        assertThat(token.expiraEm()).isEqualTo(AGORA.plusSeconds(3600));
        assertThat(service.tokenValido(token.valor())).isTrue();
        assertThat(service.extrairEmail(token.valor()))
                .isEqualTo("admin@oficina.com");
        assertThat(service.extrairPerfil(token.valor())).isEqualTo("ADMIN");
    }

    @Test
    void deveRejeitarTokenAdulterado() {
        JwtService service = serviceNoInstante(AGORA);
        String token = service.gerarToken(usuarioAdmin()).valor();
        String adulterado = token.substring(0, token.length() - 1) + "x";

        assertThat(service.tokenValido(adulterado)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService emissor = serviceNoInstante(AGORA);
        String token = emissor.gerarToken(usuarioAdmin()).valor();
        JwtService validadorFuturo = serviceNoInstante(
                AGORA.plusSeconds(3601)
        );

        assertThat(validadorFuturo.tokenValido(token)).isFalse();
    }

    private JwtService serviceNoInstante(Instant instante) {
        return new JwtService(
                SECRET,
                60,
                Clock.fixed(instante, ZoneOffset.UTC)
        );
    }

    private Usuario usuarioAdmin() {
        return Usuario.builder()
                .email("admin@oficina.com")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();
    }
}
