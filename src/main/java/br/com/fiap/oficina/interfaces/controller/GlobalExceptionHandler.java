package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.domain.common.ExcecaoDominio;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.exception.CredenciaisInvalidasException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException exception,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(criarErro(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                request.getRequestURI(),
                null
        ));
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        ErroResponse erro = criarErro(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(erro);
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResponse> tratarRegraNegocio(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        ErroResponse erro = criarErro(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> campos = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(erro -> campos.put(
                        erro.getField(),
                        erro.getDefaultMessage()
                ));

        ErroResponse resposta = criarErro(
                HttpStatus.BAD_REQUEST,
                "Existem dados inválidos na requisição",
                request.getRequestURI(),
                campos
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(resposta);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            ConstraintViolationException.class
    })
    public ResponseEntity<ErroResponse> tratarRequisicaoInvalida(
            Exception exception,
            HttpServletRequest request
    ) {
        ErroResponse erro = criarErro(
                HttpStatus.BAD_REQUEST,
                "A requisição possui parâmetros ou conteúdo inválidos",
                request.getRequestURI(),
                null
        );

        return ResponseEntity.badRequest().body(erro);
    }

    @ExceptionHandler({
            DataIntegrityViolationException.class,
            ObjectOptimisticLockingFailureException.class
    })
    public ResponseEntity<ErroResponse> tratarIntegridadeBanco(
            Exception exception,
            HttpServletRequest request
    ) {
        ErroResponse erro = criarErro(
                HttpStatus.CONFLICT,
                "A operação viola uma restrição de integridade dos dados",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResponse> tratarErroInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        log.error(
                "Erro inesperado ao processar a requisição {}",
                request.getRequestURI(),
                exception
        );

        ErroResponse erro = criarErro(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocorreu um erro interno inesperado",
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(erro);
    }

    private ErroResponse criarErro(
            HttpStatus status,
            String mensagem,
            String caminho,
            Map<String, String> campos
    ) {
        return new ErroResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensagem,
                caminho,
                campos
        );
    }

    @ExceptionHandler(ExcecaoDominio.class)
    public ResponseEntity<ErroResponse> tratarExcecaoDominio(
            ExcecaoDominio exception,
            HttpServletRequest request
    ) {
        ErroResponse erro = criarErro(
                HttpStatus.UNPROCESSABLE_ENTITY,
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(erro);
    }
}
