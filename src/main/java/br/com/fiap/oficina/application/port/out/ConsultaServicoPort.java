package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.servico.Servico;
public interface ConsultaServicoPort {
    Servico buscarEntidadePorId(Long id);
}
