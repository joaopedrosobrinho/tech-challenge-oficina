package br.com.fiap.oficina.infrastructure.repository;

import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrdemServicoRepository
        extends JpaRepository<OrdemServico, Long> {

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca"
    })
    Optional<OrdemServico> findOneById(Long id);

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca"
    })
    Optional<OrdemServico> findByNumeroOs(String numeroOs);

    @EntityGraph(attributePaths = {
            "cliente", "veiculo",
            "itensServico", "itensServico.servico",
            "itensPeca", "itensPeca.peca"
    })
    List<OrdemServico> findAllByClienteIdOrderByDataCriacaoDesc(
            Long clienteId
    );

    @EntityGraph(attributePaths = {
            "cliente", "veiculo",
            "itensServico", "itensServico.servico",
            "itensPeca", "itensPeca.peca"
    })
    List<OrdemServico> findAllByVeiculoIdOrderByDataCriacaoDesc(
            Long veiculoId
    );

    @EntityGraph(attributePaths = {
            "cliente", "veiculo",
            "itensServico", "itensServico.servico",
            "itensPeca", "itensPeca.peca"
    })
    List<OrdemServico> findAllByStatusOrderByDataCriacaoDesc(
            StatusOrdemServico status
    );

    boolean existsByNumeroOs(String numeroOs);

    boolean existsByClienteId(Long clienteId);

    boolean existsByVeiculoId(Long veiculoId);

    boolean existsByItensServicoServicoId(Long servicoId);

    boolean existsByItensPecaPecaId(Long pecaId);

    @Query("""
            SELECT os
            FROM OrdemServico os
            WHERE os.dataCriacao BETWEEN :dataInicial AND :dataFinal
            ORDER BY os.dataCriacao DESC
            """)
    List<OrdemServico> buscarPorPeriodo(
            @Param("dataInicial") LocalDateTime dataInicial,
            @Param("dataFinal") LocalDateTime dataFinal
    );

    List<OrdemServico>
    findAllByDataInicioExecucaoIsNotNullAndDataFinalizacaoIsNotNull();

    @EntityGraph(attributePaths = {
            "cliente",
            "veiculo",
            "itensServico",
            "itensServico.servico",
            "itensPeca",
            "itensPeca.peca"
    })
    @Query("SELECT DISTINCT os FROM OrdemServico os ORDER BY os.dataCriacao DESC")
    List<OrdemServico> buscarTodasComDetalhes();

    @Query("""
    SELECT DISTINCT os
    FROM OrdemServico os
    LEFT JOIN FETCH os.cliente
    LEFT JOIN FETCH os.veiculo
    LEFT JOIN FETCH os.itensServico itemServico
    LEFT JOIN FETCH itemServico.servico
    LEFT JOIN FETCH os.itensPeca itemPeca
    LEFT JOIN FETCH itemPeca.peca
    WHERE os.id = :id
""")
    Optional<OrdemServico> buscarPorIdComDetalhes(
            @Param("id") Long id
    );
}
