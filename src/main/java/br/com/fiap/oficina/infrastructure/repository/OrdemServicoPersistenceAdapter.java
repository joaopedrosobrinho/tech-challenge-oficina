package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import br.com.fiap.oficina.application.port.out.OrdemServicoStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrdemServicoPersistenceAdapter implements OrdemServicoStore {
    private final OrdemServicoRepository repository;

    @Override
    public Optional<OrdemServico> findOneById(Long id) {
        return repository.findOneById(id);
    }

    @Override
    public Optional<OrdemServico> findByNumeroOs(String numeroOs) {
        return repository.findByNumeroOs(numeroOs);
    }

    @Override
    public List<OrdemServico> findAllByClienteIdOrderByDataCriacaoDesc(Long clienteId) {
        return repository.findAllByClienteIdOrderByDataCriacaoDesc(clienteId);
    }

    @Override
    public List<OrdemServico> findAllByVeiculoIdOrderByDataCriacaoDesc(Long veiculoId) {
        return repository.findAllByVeiculoIdOrderByDataCriacaoDesc(veiculoId);
    }

    @Override
    public List<OrdemServico> findAllByStatusOrderByDataCriacaoDesc(StatusOrdemServico status) {
        return repository.findAllByStatusOrderByDataCriacaoDesc(status);
    }

    @Override
    public boolean existsByNumeroOs(String numeroOs) {
        return repository.existsByNumeroOs(numeroOs);
    }

    @Override
    public boolean existsByClienteId(Long clienteId) {
        return repository.existsByClienteId(clienteId);
    }

    @Override
    public boolean existsByVeiculoId(Long veiculoId) {
        return repository.existsByVeiculoId(veiculoId);
    }

    @Override
    public boolean existsByItensServicoServicoId(Long servicoId) {
        return repository.existsByItensServicoServicoId(servicoId);
    }

    @Override
    public boolean existsByItensPecaPecaId(Long pecaId) {
        return repository.existsByItensPecaPecaId(pecaId);
    }

    @Override
    public List<OrdemServico> buscarPorPeriodo(LocalDateTime dataInicial, LocalDateTime dataFinal) {
        return repository.buscarPorPeriodo(dataInicial, dataFinal);
    }

    @Override
    public List<OrdemServico> findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull() {
        return repository.findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull();
    }

    @Override
    public List<OrdemServico> buscarTodasComDetalhes() {
        return repository.buscarTodasComDetalhes();
    }

    @Override
    public List<OrdemServico> buscarFilaOperacional() {
        return repository.buscarFilaOperacional();
    }

    @Override
    public Optional<OrdemServico> bloquearPorId(Long id) {
        return repository.bloquearPorId(id);
    }

    @Override
    public Optional<OrdemServico> buscarPorIdComDetalhes(Long id) {
        return repository.buscarPorIdComDetalhes(id);
    }

    @Override
    public <S extends OrdemServico> S save(S entity) {
        return repository.save(entity);
    }

    @Override
    public Optional<OrdemServico> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<OrdemServico> findAll() {
        return repository.findAll();
    }

    @Override
    public void delete(OrdemServico entity) {
        repository.delete(entity);
    }
}
