package br.com.fiap.oficina.application.port.out;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrdemServicoStore {
    Optional<OrdemServico> findOneById(Long id);
    Optional<OrdemServico> findByNumeroOs(String numeroOs);
    List<OrdemServico> findAllByClienteIdOrderByDataCriacaoDesc(Long clienteId);
    List<OrdemServico> findAllByVeiculoIdOrderByDataCriacaoDesc(Long veiculoId);
    List<OrdemServico> findAllByStatusOrderByDataCriacaoDesc(StatusOrdemServico status);
    boolean existsByNumeroOs(String numeroOs);
    boolean existsByClienteId(Long clienteId);
    boolean existsByVeiculoId(Long veiculoId);
    boolean existsByItensServicoServicoId(Long servicoId);
    boolean existsByItensPecaPecaId(Long pecaId);
    List<OrdemServico> buscarPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal);
    List<OrdemServico> findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull();
    List<OrdemServico> buscarTodasComDetalhes();
    List<OrdemServico> buscarFilaOperacional();
    Optional<OrdemServico> bloquearPorId(Long id);
    Optional<OrdemServico> buscarPorIdComDetalhes(Long id);
    <S extends OrdemServico> S save(S entity);
    Optional<OrdemServico> findById(Long id);
    List<OrdemServico> findAll();
    void delete(OrdemServico entity);
}
