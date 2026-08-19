package br.com.fiap.oficina.infrastructure.security;

import br.com.fiap.oficina.domain.usuario.Usuario;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.interfaces.controller.ErroResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO_BEARER = "Bearer ";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || authorization.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!authorization.startsWith(PREFIXO_BEARER)) {
            responderNaoAutorizado(request, response);
            return;
        }

        String token = authorization.substring(PREFIXO_BEARER.length()).trim();

        if (token.isBlank() || !jwtService.tokenValido(token)) {
            responderNaoAutorizado(request, response);
            return;
        }

        try {
            autenticar(token, request);
            filterChain.doFilter(request, response);
        } catch (RuntimeException exception) {
            SecurityContextHolder.clearContext();
            responderNaoAutorizado(request, response);
        }
    }

    private void autenticar(String token, HttpServletRequest request) {
        String email = jwtService.extrairEmail(token);
        String perfilToken = jwtService.extrairPerfil(token);

        Usuario usuario = usuarioRepository
                .findByEmailIgnoreCaseAndAtivoTrue(email)
                .filter(encontrado -> encontrado.getPerfil().name()
                        .equals(perfilToken))
                .orElseThrow();

        var authentication = new UsernamePasswordAuthenticationToken(
                usuario.getEmail(),
                null,
                List.of(new SimpleGrantedAuthority(
                        "ROLE_" + usuario.getPerfil().name()
                ))
        );
        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private void responderNaoAutorizado(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ErroResponse erro = new ErroResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(),
                HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                "Token JWT inválido ou expirado",
                request.getRequestURI(),
                null
        );
        objectMapper.writeValue(response.getOutputStream(), erro);
    }
}
