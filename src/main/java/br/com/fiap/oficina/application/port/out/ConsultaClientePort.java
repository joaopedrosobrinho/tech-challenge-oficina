package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.cliente.Cliente;
public interface ConsultaClientePort {
    Cliente buscarEntidadePorId(Long id);
}
