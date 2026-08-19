package br.com.fiap.oficina.infrastructure.security;

import br.com.fiap.oficina.domain.usuario.PerfilUsuario;
import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UsuarioRepository usuarioRepository;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void configurar() {
        filter = new JwtAuthenticationFilter(
                jwtService,
                usuarioRepository,
                new ObjectMapper().findAndRegisterModules()
        );
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveProsseguirSemAutenticacaoQuandoNaoHaCabecalho() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/clientes");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertThat(chain.getRequest()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, usuarioRepository);
    }

    @Test
    void deveConfigurarContextoComBearerValido() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/clientes");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-valido");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        Usuario usuario = Usuario.builder()
                .email("admin@oficina.com")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();
        when(jwtService.tokenValido("token-valido")).thenReturn(true);
        when(jwtService.extrairEmail("token-valido"))
                .thenReturn("admin@oficina.com");
        when(jwtService.extrairPerfil("token-valido")).thenReturn("ADMIN");
        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@oficina.com"))
                .thenReturn(Optional.of(usuario));

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(chain.getRequest()).isNotNull();
        assertThat(authentication.getName()).isEqualTo("admin@oficina.com");
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }

    @Test
    void deveResponder401SemContinuarParaTokenInvalido() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/clientes");
        request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer token-invalido");
        var response = new MockHttpServletResponse();
        var chain = new MockFilterChain();
        when(jwtService.tokenValido("token-invalido")).thenReturn(false);

        filter.doFilter(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentAsString())
                .contains("Token JWT inválido ou expirado")
                .doesNotContain("token-invalido");
        assertThat(chain.getRequest()).isNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
