package br.com.fiap.oficina.application.mapper;

import br.com.fiap.oficina.application.dto.cliente.ClienteRequest;
import br.com.fiap.oficina.application.dto.cliente.ClienteResponse;
import br.com.fiap.oficina.domain.cliente.Cliente;

public final class ClienteMapper {

    private ClienteMapper() {
    }

    public static Cliente paraEntidade(ClienteRequest request) {
        if (request == null) {
            return null;
        }

        return Cliente.builder()
                .nome(request.nome().trim())
                .cpfCnpj(somenteNumeros(request.cpfCnpj()))
                .telefone(somenteNumeros(request.telefone()))
                .email(request.email().trim().toLowerCase())
                .build();
    }

    public static ClienteResponse paraResponse(Cliente cliente) {
        if (cliente == null) {
            return null;
        }

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail()
        );
    }

    public static void atualizarEntidade(
            Cliente cliente,
            ClienteRequest request
    ) {
        cliente.setNome(request.nome().trim());
        cliente.setCpfCnpj(somenteNumeros(request.cpfCnpj()));
        cliente.setTelefone(somenteNumeros(request.telefone()));
        cliente.setEmail(request.email().trim().toLowerCase());
    }

    private static String somenteNumeros(String valor) {
        return valor == null ? null : valor.replaceAll("\\D", "");
    }
}