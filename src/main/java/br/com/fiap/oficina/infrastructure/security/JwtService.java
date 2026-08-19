package br.com.fiap.oficina.infrastructure.security;

import br.com.fiap.oficina.domain.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey chave;
    private final Duration validade;
    private final Clock clock;

    @Autowired
    public JwtService(
            @Value("${oficina.security.jwt.secret}") String secret,
            @Value("${oficina.security.jwt.expiration-minutes:60}") long expirationMinutes
    ) {
        this(secret, expirationMinutes, Clock.systemUTC());
    }

    JwtService(String secret, long expirationMinutes, Clock clock) {
        if (expirationMinutes <= 0) {
            throw new IllegalArgumentException("A validade do JWT deve ser maior que zero");
        }

        this.chave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.validade = Duration.ofMinutes(expirationMinutes);
        this.clock = clock;
    }

    public TokenGerado gerarToken(Usuario usuario) {
        Instant emitidoEm = clock.instant();
        Instant expiraEm = emitidoEm.plus(validade);

        String token = Jwts.builder()
                .issuer("tech-challenge-oficina")
                .subject(usuario.getEmail())
                .claim("perfil", usuario.getPerfil().name())
                .issuedAt(Date.from(emitidoEm))
                .expiration(Date.from(expiraEm))
                .signWith(chave)
                .compact();

        return new TokenGerado(token, expiraEm);
    }

    public boolean tokenValido(String token) {
        try {
            Claims claims = extrairClaims(token);
            return claims.getSubject() != null
                    && claims.getExpiration().toInstant().isAfter(clock.instant());
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extrairEmail(String token) {
        return extrairClaims(token).getSubject();
    }

    public String extrairPerfil(String token) {
        return extrairClaims(token).get("perfil", String.class);
    }

    private Claims extrairClaims(String token) {
        return Jwts.parser()
                .verifyWith(chave)
                .clock(() -> Date.from(clock.instant()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public record TokenGerado(String valor, Instant expiraEm) {
    }
}
