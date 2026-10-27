package br.com.fiap.oficina.application.port.out;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
public interface NotificacaoPort {
    void registrarMudanca(OrdemServico ordem);
}
