package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.service.OrdemServicoService;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import br.com.fiap.oficina.application.dto.ordemservico.DuracaoExecucaoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.TempoMedioExecucaoResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@ExtendWith(MockitoExtension.class)
class OrdemServicoControllerTest {

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private OrdemServicoController ordemServicoController;

    @Test
    void deveDelegarFinalizacaoParaService() {
        ResponseEntity<?> resposta = ordemServicoController.finalizar(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).finalizar(1L);
    }

    @Test
    void deveDelegarEntregaParaService() {
        ResponseEntity<?> resposta = ordemServicoController.entregar(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).entregar(1L);
    }

    @Test
    void deveDelegarCancelamentoParaService() {
        ResponseEntity<?> resposta = ordemServicoController.cancelar(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).cancelar(1L);
    }

    @Test
    void deveDelegarRemocaoDePecaParaService() {
        ResponseEntity<?> resposta = ordemServicoController.removerPeca(1L, 2L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).removerPeca(1L, 2L);
    }

    @Test
    void deveDelegarConsultaPorClienteParaService() {
        when(ordemServicoService.listarPorCliente(10L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta = ordemServicoController.listarPorCliente(10L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).listarPorCliente(10L);
    }

    @Test
    void deveDelegarConsultaPorVeiculoParaService() {
        when(ordemServicoService.listarPorVeiculo(20L))
                .thenReturn(List.of());

        ResponseEntity<?> resposta = ordemServicoController.listarPorVeiculo(20L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).listarPorVeiculo(20L);
    }

    @Test
    void deveDelegarConsultaPorStatusParaService() {
        when(ordemServicoService.listarPorStatus(StatusOrdemServico.FINALIZADA))
                .thenReturn(List.of());

        ResponseEntity<?> resposta = ordemServicoController
                .listarPorStatus(StatusOrdemServico.FINALIZADA);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        verify(ordemServicoService).listarPorStatus(StatusOrdemServico.FINALIZADA);
    }

    @Test
    void deveDelegarConsultaDeDuracaoParaService() {
        var duracao = new DuracaoExecucaoResponse(
                1L,
                "OS-123",
                StatusOrdemServico.EM_EXECUCAO,
                LocalDateTime.now().minusMinutes(10),
                LocalDateTime.now(),
                600L,
                10L,
                true
        );
        when(ordemServicoService.consultarDuracaoExecucao(1L))
                .thenReturn(duracao);

        ResponseEntity<?> resposta = ordemServicoController
                .consultarDuracaoExecucao(1L);

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(duracao, resposta.getBody());
        verify(ordemServicoService).consultarDuracaoExecucao(1L);
    }

    @Test
    void deveDelegarConsultaDeTempoMedioParaService() {
        var indicador = new TempoMedioExecucaoResponse(
                2,
                new BigDecimal("5400.00"),
                new BigDecimal("90.00"),
                LocalDateTime.now()
        );
        when(ordemServicoService.consultarTempoMedioExecucao())
                .thenReturn(indicador);

        ResponseEntity<?> resposta = ordemServicoController
                .consultarTempoMedioExecucao();

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(indicador, resposta.getBody());
        verify(ordemServicoService).consultarTempoMedioExecucao();
    }
}
