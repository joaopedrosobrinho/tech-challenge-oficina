package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.common.ExcecaoDominio;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingServletRequestParameterException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void configurar() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/teste");
    }

    @Test
    void devePadronizarRecursoNaoEncontradoComo404() {
        var resposta = handler.tratarRecursoNaoEncontrado(
                new RecursoNaoEncontradoException("Não encontrado"),
                request
        );

        assertEquals(HttpStatus.NOT_FOUND, resposta.getStatusCode());
        assertEquals(404, resposta.getBody().status());
        assertEquals("/api/teste", resposta.getBody().caminho());
    }

    @Test
    void devePadronizarRegraDeNegocioComo409() {
        var resposta = handler.tratarRegraNegocio(
                new RegraNegocioException("Transição inválida"),
                request
        );

        assertEquals(HttpStatus.CONFLICT, resposta.getStatusCode());
        assertEquals(409, resposta.getBody().status());
    }

    @Test
    void devePadronizarExcecaoDeDominioComo422() {
        var resposta = handler.tratarExcecaoDominio(
                new ExcecaoDominio("Entidade inválida"),
                request
        );

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, resposta.getStatusCode());
        assertEquals(422, resposta.getBody().status());
    }

    @Test
    void devePadronizarParametroAusenteComo400() {
        var resposta = handler.tratarRequisicaoInvalida(
                new MissingServletRequestParameterException("placa", "String"),
                request
        );

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(400, resposta.getBody().status());
    }

    @Test
    void naoDeveExporMensagemInternaNoErro500() {
        var resposta = handler.tratarErroInesperado(
                new RuntimeException("senha-secreta-do-banco"),
                request
        );

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, resposta.getStatusCode());
        assertNotNull(resposta.getBody().dataHora());
        assertFalse(resposta.getBody().mensagem().contains("senha-secreta"));
        assertEquals("Ocorreu um erro interno inesperado", resposta.getBody().mensagem());
    }
}
