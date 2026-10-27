package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.usuario.Usuario;
import java.time.Instant;
public interface TokenPort {
    TokenGerado gerarToken(Usuario usuario);
    record TokenGerado(String valor, Instant expiraEm) {}
}
