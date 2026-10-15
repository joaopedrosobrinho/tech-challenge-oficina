package br.com.fiap.oficina.application.port.out;
import java.util.Optional;
import java.util.UUID;
public interface AprovacaoExternaPort {
    Optional<Decisao> buscar(UUID eventoId);
    void registrar(Decisao decisao);
    record Decisao(UUID eventoId, Long ordemId, Long versaoOrcamento, boolean aprovada, String observacao) {}
}
