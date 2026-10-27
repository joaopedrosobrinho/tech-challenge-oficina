package br.com.fiap.oficina.infrastructure.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "oficina.notificacoes.enabled", havingValue = "true")
public class NotificacaoWorker {
    private final JdbcTemplate jdbc;
    private final JavaMailSender mail;
    @Value("${oficina.notificacoes.remetente}")
    private String remetente;

    @Scheduled(fixedDelayString = "${oficina.notificacoes.intervalo-ms:10000}")
    @Transactional
    public void enviarPendentes() {
        // Each replica claims different rows; failed deliveries remain available for retry.
        var pendentes = jdbc.queryForList("""
                SELECT id, destinatario, assunto, mensagem FROM notificacoes
                WHERE enviada_em IS NULL AND proxima_tentativa <= CURRENT_TIMESTAMP
                ORDER BY id LIMIT 10 FOR UPDATE SKIP LOCKED
                """);
        for (var registro : pendentes) {
            long id = ((Number) registro.get("id")).longValue();
            SimpleMailMessage mensagem = new SimpleMailMessage();
            mensagem.setFrom(remetente);
            mensagem.setTo((String) registro.get("destinatario"));
            mensagem.setSubject((String) registro.get("assunto"));
            mensagem.setText((String) registro.get("mensagem"));
            try {
                mail.send(mensagem);
                jdbc.update("UPDATE notificacoes SET enviada_em = CURRENT_TIMESTAMP, tentativas = tentativas + 1 WHERE id = ?", id);
            } catch (MailException ex) {
                jdbc.update("""
                        UPDATE notificacoes SET tentativas = tentativas + 1,
                        proxima_tentativa = ? WHERE id = ?
                        """, java.sql.Timestamp.from(java.time.Instant.now().plusSeconds(60)), id);
                log.warn("Falha no envio da notificação {}; nova tentativa em 60 segundos", id);
            }
        }
    }
}
