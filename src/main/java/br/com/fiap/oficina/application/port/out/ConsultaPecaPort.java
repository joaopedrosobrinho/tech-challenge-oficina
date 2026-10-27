package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.peca.Peca;
public interface ConsultaPecaPort {
    Peca buscarEntidadePorId(Long id);
}
