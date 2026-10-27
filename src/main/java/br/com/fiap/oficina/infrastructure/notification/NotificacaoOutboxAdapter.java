package br.com.fiap.oficina.infrastructure.notification;

import br.com.fiap.oficina.application.port.out.NotificacaoPort;
import br.com.fiap.oficina.domain.ordemservico.OrdemServico;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificacaoOutboxAdapter implements NotificacaoPort {
    private final JdbcTemplate jdbc;

    @Override
    public void registrarMudanca(OrdemServico ordem) {
        String mensagem = "A ordem " + ordem.getNumeroOs() + " está no status " + ordem.getStatus() + ".";
        if (ordem.getStatus() == br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico.AGUARDANDO_APROVACAO) {
            mensagem += " Versão do orçamento: " + ordem.getVersaoOrcamento() + ".";
        }
        jdbc.update("""
                INSERT INTO notificacoes (ordem_id, destinatario, assunto, mensagem)
                VALUES (?, ?, ?, ?)
                """, ordem.getId(), ordem.getCliente().getEmail(), "Atualização da OS " + ordem.getNumeroOs(), mensagem);
    }
}
