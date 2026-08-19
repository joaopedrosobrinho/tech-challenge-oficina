package br.com.fiap.oficina.application.dto.cliente;

public record ClienteResponse(
        Long id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email
) {
}