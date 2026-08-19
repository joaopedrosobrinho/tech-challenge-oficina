package br.com.fiap.oficina.application.dto.autenticacao;

import br.com.fiap.oficina.domain.usuario.PerfilUsuario;

import java.time.Instant;

public record LoginResponse(
        String email,
        PerfilUsuario perfil,
        String tipo,
        String token,
        Instant expiraEm
) {
}
