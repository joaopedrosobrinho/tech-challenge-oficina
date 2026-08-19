package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoResponse;
import br.com.fiap.oficina.application.service.OrdemServicoService;
import br.com.fiap.oficina.application.dto.ordemservico.OrcamentoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarPecaOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarServicoOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.DuracaoExecucaoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.TempoMedioExecucaoResponse;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@RequiredArgsConstructor
@Tag(name = "Ordens de serviço", description = "Fluxo completo da ordem de serviço, orçamento, execução e indicadores")
@Validated
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    @PostMapping
    @Operation(summary = "Criar ordem de serviço", description = "Cria a OS e calcula o orçamento inicial a partir dos itens informados.")
    public ResponseEntity<OrdemServicoResponse> criar(
            @Valid @RequestBody OrdemServicoRequest request
    ) {
        OrdemServicoResponse response =
                ordemServicoService.criar(request);

        URI localizacao = URI.create(
                "/api/ordens-servico/" + response.id()
        );

        return ResponseEntity
                .created(localizacao)
                .body(response);
    }

    @GetMapping
    @Operation(summary = "Listar ordens de serviço")
    public ResponseEntity<List<OrdemServicoResponse>> listarTodas() {
        return ResponseEntity.ok(
                ordemServicoService.listarTodas()
        );
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar ordem de serviço por ID")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.buscarPorId(id)
        );
    }

    @GetMapping("/numero/{numeroOs}")
    @Operation(summary = "Buscar ordem de serviço pelo número")
    public ResponseEntity<OrdemServicoResponse> buscarPorNumero(
            @NotBlank @PathVariable String numeroOs
    ) {
        return ResponseEntity.ok(
                ordemServicoService.buscarPorNumero(numeroOs)
        );
    }

    @GetMapping("/{id}/orcamento")
    @Operation(summary = "Consultar orçamento da OS")
    public ResponseEntity<OrcamentoResponse> consultarOrcamento(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.consultarOrcamento(id)
        );
    }

    @PostMapping("/{id}/orcamento/recalcular")
    @Operation(summary = "Recalcular orçamento")
    public ResponseEntity<OrcamentoResponse> recalcularOrcamento(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.calcularOrcamento(id)
        );
    }

    @PostMapping("/{id}/servicos")
    @Operation(summary = "Adicionar serviço à OS", description = "Inclui ou incrementa um serviço antes da execução.")
    public ResponseEntity<OrdemServicoResponse> adicionarServico(
            @Positive @PathVariable Long id,
            @Valid @RequestBody
            AdicionarServicoOrdemRequest request
    ) {
        OrdemServicoResponse response =
                ordemServicoService.adicionarServico(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/pecas")
    @Operation(summary = "Adicionar peça à OS", description = "Inclui ou incrementa uma peça antes da execução.")
    public ResponseEntity<OrdemServicoResponse> adicionarPeca(
            @Positive @PathVariable Long id,
            @Valid @RequestBody
            AdicionarPecaOrdemRequest request
    ) {
        OrdemServicoResponse response =
                ordemServicoService.adicionarPeca(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/orcamento/enviar-aprovacao")
    @Operation(summary = "Enviar orçamento para aprovação")
    public ResponseEntity<AprovacaoOrcamentoResponse>
    enviarOrcamentoParaAprovacao(
            @Positive @PathVariable Long id
    ) {
        AprovacaoOrcamentoResponse response =
                ordemServicoService.enviarParaAprovacao(id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/orcamento/aprovar")
    @Operation(summary = "Aprovar orçamento", description = "Inicia a execução e realiza a baixa automática do estoque.")
    public ResponseEntity<AprovacaoOrcamentoResponse>
    aprovarOrcamento(
            @Positive @PathVariable Long id,
            @Valid
            @RequestBody(required = false)
            AprovacaoOrcamentoRequest request
    ) {
        AprovacaoOrcamentoResponse response =
                ordemServicoService.aprovarOrcamento(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/orcamento/recusar")
    @Operation(summary = "Recusar orçamento", description = "Retorna a OS ao diagnóstico; a observação da recusa é obrigatória.")
    public ResponseEntity<AprovacaoOrcamentoResponse>
    recusarOrcamento(
            @Positive @PathVariable Long id,
            @Valid
            @RequestBody(required = false)
            AprovacaoOrcamentoRequest request
    ) {
        AprovacaoOrcamentoResponse response =
                ordemServicoService.recusarOrcamento(
                        id,
                        request
                );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}/pecas/{pecaId}")
    @Operation(summary = "Remover peça da OS", description = "Remove uma peça antes do início da execução.")
    public ResponseEntity<OrdemServicoResponse> removerPeca(
            @Positive @PathVariable Long id,
            @Positive @PathVariable Long pecaId
    ) {
        return ResponseEntity.ok(
                ordemServicoService.removerPeca(id, pecaId)
        );
    }

    @GetMapping("/{id}/duracao-execucao")
    @Operation(summary = "Consultar duração da execução")
    public ResponseEntity<DuracaoExecucaoResponse> consultarDuracaoExecucao(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.consultarDuracaoExecucao(id)
        );
    }

    @GetMapping("/cliente/{clienteId}")
    @Operation(summary = "Listar ordens por cliente")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorCliente(
            @Positive @PathVariable Long clienteId
    ) {
        return ResponseEntity.ok(
                ordemServicoService.listarPorCliente(clienteId)
        );
    }

    @GetMapping("/veiculo/{veiculoId}")
    @Operation(summary = "Listar ordens por veículo")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorVeiculo(
            @Positive @PathVariable Long veiculoId
    ) {
        return ResponseEntity.ok(
                ordemServicoService.listarPorVeiculo(veiculoId)
        );
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar ordens por status")
    public ResponseEntity<List<OrdemServicoResponse>> listarPorStatus(
            @PathVariable StatusOrdemServico status
    ) {
        return ResponseEntity.ok(
                ordemServicoService.listarPorStatus(status)
        );
    }

    @GetMapping("/indicadores/tempo-medio-execucao")
    @Operation(summary = "Consultar tempo médio de execução")
    public ResponseEntity<TempoMedioExecucaoResponse>
    consultarTempoMedioExecucao() {
        return ResponseEntity.ok(
                ordemServicoService.consultarTempoMedioExecucao()
        );
    }

    @PostMapping("/{id}/finalizar")
    @Operation(summary = "Finalizar ordem de serviço")
    public ResponseEntity<OrdemServicoResponse> finalizar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.finalizar(id)
        );
    }

    @PostMapping("/{id}/entregar")
    @Operation(summary = "Registrar entrega do veículo")
    public ResponseEntity<OrdemServicoResponse> entregar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.entregar(id)
        );
    }

    @PostMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar ordem de serviço", description = "Quando aplicável, devolve automaticamente ao estoque as peças já baixadas.")
    public ResponseEntity<OrdemServicoResponse> cancelar(
            @Positive @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                ordemServicoService.cancelar(id)
        );
    }
}
