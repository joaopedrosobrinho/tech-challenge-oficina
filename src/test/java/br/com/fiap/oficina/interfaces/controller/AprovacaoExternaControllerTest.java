package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.service.AprovacaoExternaService;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.infrastructure.security.IntegracaoSecurityConfig;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(value = AprovacaoExternaController.class,
        properties = "oficina.security.integracao-token=token-exclusivo-para-teste-32-caracteres")
@Import({SecurityConfig.class, IntegracaoSecurityConfig.class, SecurityErrorHandler.class})
class AprovacaoExternaControllerTest {
    private static final String URL = "/api/integracoes/ordens-servico/1/aprovacao";
    private static final String TOKEN = "token-exclusivo-para-teste-32-caracteres";
    private static final String BODY = """
            {"eventoId":"89236a1f-61e5-4d14-b368-047b54fbff50","versaoOrcamento":1,"aprovada":true}
            """;
    @Autowired MockMvc mvc;
    @MockitoBean AprovacaoExternaService service;
    @MockitoBean JwtService jwt;
    @MockitoBean UsuarioRepository usuarios;

    @Test
    void exigeCredencialPropriaMesmoComAdministradorAutenticado() throws Exception {
        mvc.perform(post(URL).with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void rejeitaTokenInvalido() throws Exception {
        mvc.perform(post(URL).header("X-Integration-Key", "incorreto")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(service);
    }

    @Test
    void aceitaNotificacaoAutenticada() throws Exception {
        mvc.perform(post(URL).header("X-Integration-Key", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content(BODY))
                .andExpect(status().isNoContent());
        verify(service).receber(eq(1L), any());
    }

    @Test
    void rejeitaPayloadSemEventoOuVersao() throws Exception {
        mvc.perform(post(URL).header("X-Integration-Key", TOKEN)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"aprovada\":true}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
