package br.com.fiap.oficina.infrastructure.security;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;

@Configuration
public class IntegracaoSecurityConfig {
    @Bean
    @Order(1)
    SecurityFilterChain integracao(HttpSecurity http,
            @Value("${oficina.security.integracao-token:}") String token) throws Exception {
        var filter = new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain chain) throws ServletException, IOException {
                String recebido = request.getHeader("X-Integration-Key");
                if (token.length() < 32 || recebido == null || !MessageDigest.isEqual(
                        token.getBytes(StandardCharsets.UTF_8), recebido.getBytes(StandardCharsets.UTF_8))) {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"mensagem\":\"Credencial de integração inválida\"}");
                    return;
                }
                chain.doFilter(request, response);
            }
        };
        return http.securityMatcher("/api/integracoes/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class).build();
    }
}
