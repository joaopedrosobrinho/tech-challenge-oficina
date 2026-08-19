package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.ordemservico.AcompanhamentoOrdemServicoResponse;
import br.com.fiap.oficina.application.service.OrdemServicoService;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AcompanhamentoOrdemServicoControllerTest {

    @Mock
    private OrdemServicoService ordemServicoService;

    @InjectMocks
    private AcompanhamentoOrdemServicoController controller;

    @Test
    void deveDelegarAcompanhamentoPublicoParaService() {
        var acompanhamento = new AcompanhamentoOrdemServicoResponse(
                "OS-123",
                StatusOrdemServico.EM_DIAGNOSTICO,
                "Veículo em diagnóstico",
                null, null, null, null, null
        );
        when(ordemServicoService.acompanharPublicamente("OS-123", "ABC1D23"))
                .thenReturn(acompanhamento);

        var resposta = controller.acompanhar("OS-123", "ABC1D23");

        assertEquals(HttpStatus.OK, resposta.getStatusCode());
        assertEquals(acompanhamento, resposta.getBody());
        verify(ordemServicoService)
                .acompanharPublicamente("OS-123", "ABC1D23");
    }
}
