package br.com.fiap.oficina.application.mapper;

import br.com.fiap.oficina.application.dto.veiculo.VeiculoRequest;
import br.com.fiap.oficina.application.dto.veiculo.VeiculoResponse;
import br.com.fiap.oficina.application.validation.PlacaValidator;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.veiculo.Veiculo;

public final class VeiculoMapper {

    private VeiculoMapper() {
    }

    public static Veiculo paraEntidade(
            VeiculoRequest request,
            Cliente cliente
    ) {
        if (request == null) {
            return null;
        }

        return Veiculo.builder()
                .placa(normalizarPlaca(request.placa()))
                .marca(request.marca().trim())
                .modelo(request.modelo().trim())
                .ano(request.ano())
                .cliente(cliente)
                .build();
    }

    public static VeiculoResponse paraResponse(Veiculo veiculo) {
        if (veiculo == null) {
            return null;
        }

        Cliente cliente = veiculo.getCliente();

        return new VeiculoResponse(
                veiculo.getId(),
                veiculo.getPlaca(),
                veiculo.getMarca(),
                veiculo.getModelo(),
                veiculo.getAno(),
                cliente != null ? cliente.getId() : null,
                cliente != null ? cliente.getNome() : null
        );
    }

    public static void atualizarEntidade(
            Veiculo veiculo,
            VeiculoRequest request,
            Cliente cliente
    ) {
        veiculo.setPlaca(normalizarPlaca(request.placa()));
        veiculo.setMarca(request.marca().trim());
        veiculo.setModelo(request.modelo().trim());
        veiculo.setAno(request.ano());
        veiculo.setCliente(cliente);
    }

    private static String normalizarPlaca(String placa) {
        return PlacaValidator.normalizar(placa);
    }
}
