package br.com.fiap.oficina.infrastructure.repository;
import br.com.fiap.oficina.application.port.out.AprovacaoExternaPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;
@Repository
@RequiredArgsConstructor
public class AprovacaoExternaAdapter implements AprovacaoExternaPort {
    private final JdbcTemplate jdbc;
    public Optional<Decisao> buscar(UUID eventoId) {
        return jdbc.query("SELECT * FROM aprovacoes_externas WHERE evento_id = ?",
                (rs, row) -> new Decisao(rs.getObject("evento_id", UUID.class), rs.getLong("ordem_id"),
                        rs.getLong("versao_orcamento"), rs.getBoolean("aprovada"), rs.getString("observacao")), eventoId)
                .stream().findFirst();
    }
    public void registrar(Decisao decisao) {
        jdbc.update("""
                INSERT INTO aprovacoes_externas (evento_id, ordem_id, versao_orcamento, aprovada, observacao)
                VALUES (?, ?, ?, ?, ?)
                """, decisao.eventoId(), decisao.ordemId(), decisao.versaoOrcamento(), decisao.aprovada(), decisao.observacao());
    }
}
