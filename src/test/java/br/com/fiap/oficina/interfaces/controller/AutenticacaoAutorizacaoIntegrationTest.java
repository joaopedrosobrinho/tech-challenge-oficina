package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.autenticacao.LoginResponse;
import br.com.fiap.oficina.application.exception.CredenciaisInvalidasException;
import br.com.fiap.oficina.application.service.AutenticacaoService;
import br.com.fiap.oficina.application.service.ClienteService;
import br.com.fiap.oficina.application.service.OrdemServicoService;
import br.com.fiap.oficina.domain.usuario.PerfilUsuario;
import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.infrastructure.security.JwtAuthenticationFilter;
import br.com.fiap.oficina.infrastructure.security.JwtService;
import br.com.fiap.oficina.infrastructure.security.SecurityConfig;
import br.com.fiap.oficina.infrastructure.security.SecurityErrorHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        AutenticacaoController.class,
        ClienteController.class,
        AcompanhamentoOrdemServicoController.class
})
@Import({
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        SecurityErrorHandler.class,
        GlobalExceptionHandler.class
})
class AutenticacaoAutorizacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticacaoService autenticacaoService;

    @MockitoBean
    private ClienteService clienteService;

    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveRealizarLoginValidoSemTokenAnterior() throws Exception {
        when(autenticacaoService.autenticar(any())).thenReturn(
                new LoginResponse(
                        "admin@oficina.com",
                        PerfilUsuario.ADMIN,
                        "Bearer",
                        "jwt-valido",
                        Instant.parse("2026-08-18T23:00:00Z")
                )
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@oficina.com",
                                  "senha": "admin123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.token").value("jwt-valido"));
    }

    @Test
    void deveRejeitarLoginInvalido() throws Exception {
        when(autenticacaoService.autenticar(any()))
                .thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "admin@oficina.com",
                                  "senha": "incorreta"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem")
                        .value("E-mail ou senha inválidos"));
    }

    @Test
    void deveBloquearEndpointAdministrativoSemToken() throws Exception {
        mockMvc.perform(get("/api/clientes"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem")
                        .value("É necessário informar um token JWT válido"));
    }

    @Test
    void devePermitirEndpointAdministrativoComTokenValido() throws Exception {
        configurarTokenValido("jwt-valido");
        when(clienteService.listarTodos()).thenReturn(List.of());

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer jwt-valido"))
                .andExpect(status().isOk());
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        when(jwtService.tokenValido("jwt-invalido")).thenReturn(false);

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer jwt-invalido"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem")
                        .value("Token JWT inválido ou expirado"));
    }

    @Test
    void deveRejeitarTokenExpirado() throws Exception {
        when(jwtService.tokenValido("jwt-expirado")).thenReturn(false);

        mockMvc.perform(get("/api/clientes")
                        .header("Authorization", "Bearer jwt-expirado"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void devePermitirConsultaPublicaSemToken() throws Exception {
        mockMvc.perform(get("/api/publico/ordens-servico/OS-123")
                        .param("placa", "ABC1D23"))
                .andExpect(status().isOk());
    }

    private void configurarTokenValido(String token) {
        Usuario usuario = Usuario.builder()
                .email("admin@oficina.com")
                .perfil(PerfilUsuario.ADMIN)
                .ativo(true)
                .build();
        when(jwtService.tokenValido(token)).thenReturn(true);
        when(jwtService.extrairEmail(token)).thenReturn("admin@oficina.com");
        when(jwtService.extrairPerfil(token)).thenReturn("ADMIN");
        when(usuarioRepository.findByEmailIgnoreCaseAndAtivoTrue("admin@oficina.com"))
                .thenReturn(Optional.of(usuario));
    }
}
