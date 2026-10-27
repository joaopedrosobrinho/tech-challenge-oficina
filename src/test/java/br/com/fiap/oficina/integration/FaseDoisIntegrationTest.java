package br.com.fiap.oficina.integration;

import br.com.fiap.oficina.application.dto.ordemservico.*;
import br.com.fiap.oficina.application.service.*;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import br.com.fiap.oficina.domain.ordemservico.*;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.domain.common.RegraNegocioException;
import br.com.fiap.oficina.infrastructure.config.UsuarioAdminInitializer;
import br.com.fiap.oficina.infrastructure.notification.NotificacaoWorker;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = {
    "spring.datasource.url=${TEST_DATABASE_URL:jdbc:h2:mem:fase2;MODE=PostgreSQL;DB_CLOSE_DELAY=-1}",
    "spring.datasource.driver-class-name=${TEST_DATABASE_DRIVER:org.h2.Driver}",
    "spring.datasource.username=${TEST_DATABASE_USER:sa}",
    "spring.datasource.password=${TEST_DATABASE_PASSWORD:}",
    "oficina.notificacoes.enabled=true", "oficina.notificacoes.intervalo-ms=3600000"
})
@Transactional
@org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
class FaseDoisIntegrationTest {
    @Autowired org.springframework.test.web.servlet.MockMvc mvc;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;
    @Autowired OrdemServicoService ordens;
    @Autowired AprovacaoExternaService integracao;
    @Autowired NotificacaoWorker worker;
    @MockitoBean UsuarioAdminInitializer initializer;
    @MockitoBean JavaMailSender mail;

    @Test
    void publicaContratoCompletoDasApis() throws Exception {
        var response = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(response.contains("/api/integracoes/ordens-servico/{id}/aprovacao"));
        assertTrue(response.contains("versaoOrcamento"));
        java.nio.file.Files.writeString(java.nio.file.Path.of("target/openapi.json"), response);
    }

    private OrdemServico ordem(StatusOrdemServico status, LocalDateTime criada) {
        String unique = UUID.randomUUID().toString();
        Cliente cliente = Cliente.builder().nome("Cliente teste").cpfCnpj(unique.substring(0,14))
                .email(unique + "@example.com").telefone("11912345678").build();
        em.persist(cliente);
        Veiculo veiculo = Veiculo.builder().cliente(cliente).placa(unique.substring(0,7))
                .marca("Fiat").modelo("Uno").ano(2020).build();
        em.persist(veiculo);
        OrdemServico ordem = OrdemServico.builder().cliente(cliente).veiculo(veiculo)
                .numeroOs(unique.substring(0,30)).status(status).dataCriacao(criada).build();
        em.persist(ordem);
        return ordem;
    }

    private OrdemServico aguardando() {
        OrdemServico ordem = ordem(StatusOrdemServico.RECEBIDA, LocalDateTime.now());
        Servico servico = Servico.builder().nome("Revisão").valor(new BigDecimal("100"))
                .tempoEstimadoMinutos(30).build();
        em.persist(servico);
        Peca peca = Peca.builder().codigo(UUID.randomUUID().toString()).nome("Filtro")
                .valor(new BigDecimal("25")).quantidadeEstoque(10).build();
        em.persist(peca);
        ordem.adicionarServico(ItemServico.builder().servico(servico).quantidade(1).valorUnitario(servico.getValor()).build());
        ordem.adicionarPeca(ItemPeca.builder().peca(peca).quantidade(2).valorUnitario(peca.getValor()).build());
        ordem.enviarParaAprovacao();
        em.flush();
        return ordem;
    }

    @Test
    void listaPrioridadeAntiguidadeEDesempateSemExcluirHistorico() {
        var agora = LocalDateTime.now();
        var recebida = ordem(StatusOrdemServico.RECEBIDA, agora.minusDays(10));
        var diagnostico = ordem(StatusOrdemServico.EM_DIAGNOSTICO, agora.minusDays(9));
        var aprovacao = ordem(StatusOrdemServico.AGUARDANDO_APROVACAO, agora.minusDays(8));
        var recente = ordem(StatusOrdemServico.EM_EXECUCAO, agora);
        var antiga = ordem(StatusOrdemServico.EM_EXECUCAO, agora.minusDays(1));
        var empate = ordem(StatusOrdemServico.EM_EXECUCAO, antiga.getDataCriacao());
        var finalizada = ordem(StatusOrdemServico.FINALIZADA, agora);
        ordem(StatusOrdemServico.ENTREGUE, agora);
        ordem(StatusOrdemServico.CANCELADA, agora);
        em.flush(); em.clear();
        assertEquals(List.of(antiga.getId(), empate.getId(), recente.getId(), aprovacao.getId(), diagnostico.getId(), recebida.getId()),
                ordens.listarTodas().stream().map(OrdemServicoResponse::id).toList());
        assertEquals(StatusOrdemServico.FINALIZADA, ordens.buscarPorId(finalizada.getId()).status());
    }

    @Test
    void aprovaUmaVezBaixaEstoqueEGravaNotificacao() {
        var ordem = aguardando();
        var evento = new DecisaoExternaRequest(UUID.randomUUID(), 1L, true, "Autorizado");
        integracao.receber(ordem.getId(), evento);
        integracao.receber(ordem.getId(), evento);
        em.flush(); em.clear();
        var atual = em.find(OrdemServico.class, ordem.getId());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, atual.getStatus());
        assertEquals(8, atual.getItensPeca().iterator().next().getPeca().getQuantidadeEstoque());
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM aprovacoes_externas WHERE ordem_id = ?", Integer.class, ordem.getId()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM notificacoes WHERE ordem_id = ?", Integer.class, ordem.getId()));
    }

    @Test
    void rejeitaVersaoAntigaDoOrcamento() {
        var ordem = aguardando();
        assertThrows(RegraNegocioException.class, () -> integracao.receber(ordem.getId(),
                new DecisaoExternaRequest(UUID.randomUUID(), 2L, false, "Valor")));
    }

    @Test
    void recusaExigeMotivoSemAlterarStatusOuEstoque() {
        var ordem = aguardando();
        assertThrows(RegraNegocioException.class, () -> integracao.receber(ordem.getId(),
                new DecisaoExternaRequest(UUID.randomUUID(), 1L, false, null)));
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordem.getStatus());
        assertEquals(10, ordem.getItensPeca().iterator().next().getPeca().getQuantidadeEstoque());
    }

    @Test
    void estoqueInsuficienteImpedeAprovacaoEEvento() {
        var ordem = aguardando();
        ordem.getItensPeca().iterator().next().getPeca().setQuantidadeEstoque(1);
        assertThrows(RegraNegocioException.class, () -> integracao.receber(ordem.getId(),
                new DecisaoExternaRequest(UUID.randomUUID(), 1L, true, null)));
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, ordem.getStatus());
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM aprovacoes_externas WHERE ordem_id = ?", Integer.class, ordem.getId()));
    }

    @Test
    void recusaPermiteNovaVersaoSemBaixarEstoque() {
        var ordem = aguardando();
        integracao.receber(ordem.getId(), new DecisaoExternaRequest(UUID.randomUUID(), 1L, false, "Rever valor"));
        assertEquals(StatusOrdemServico.EM_DIAGNOSTICO, ordem.getStatus());
        assertEquals(10, ordem.getItensPeca().iterator().next().getPeca().getQuantidadeEstoque());
        var resposta = ordens.enviarParaAprovacao(ordem.getId());
        assertEquals(2L, resposta.versaoOrcamento());
    }

    @Test
    void rejeitaEventoReutilizadoComOutraDecisao() {
        var ordem = aguardando();
        UUID id = UUID.randomUUID();
        integracao.receber(ordem.getId(), new DecisaoExternaRequest(id, 1L, true, null));
        assertThrows(RegraNegocioException.class, () -> integracao.receber(ordem.getId(),
                new DecisaoExternaRequest(id, 1L, false, "Recusa")));
    }

    @Test
    void enviaNotificacaoEPreservaFalhaParaNovaTentativa() {
        var ordem = aguardando();
        ordens.aprovarOrcamento(ordem.getId(), new AprovacaoOrcamentoRequest("Aprovado"));
        doThrow(new MailSendException("Indisponível")).when(mail).send(any(SimpleMailMessage.class));
        worker.enviarPendentes();
        assertEquals(1, jdbc.queryForObject("SELECT tentativas FROM notificacoes WHERE ordem_id = ?", Integer.class, ordem.getId()));
        assertNull(jdbc.queryForObject("SELECT enviada_em FROM notificacoes WHERE ordem_id = ?", java.sql.Timestamp.class, ordem.getId()));
        reset(mail);
        jdbc.update("UPDATE notificacoes SET proxima_tentativa = CURRENT_TIMESTAMP WHERE ordem_id = ?", ordem.getId());
        worker.enviarPendentes();
        verify(mail).send(any(SimpleMailMessage.class));
        assertNotNull(jdbc.queryForObject("SELECT enviada_em FROM notificacoes WHERE ordem_id = ?", java.sql.Timestamp.class, ordem.getId()));
    }
}
