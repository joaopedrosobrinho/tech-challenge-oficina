package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.ordemservico.DecisaoExternaRequest;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.port.out.AprovacaoExternaPort;
import br.com.fiap.oficina.application.port.out.NotificacaoPort;
import br.com.fiap.oficina.application.port.out.OrdemServicoStore;
import br.com.fiap.oficina.domain.common.RegraNegocioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AprovacaoExternaService {
    private final OrdemServicoStore ordens;
    private final AprovacaoExternaPort eventos;
    private final NotificacaoPort notificacoes;

    @Transactional
    public void receber(Long ordemId, DecisaoExternaRequest request) {
        var ordem = ordens.bloquearPorId(ordemId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Ordem de serviço não encontrada"));
        String observacao = request.observacao() == null || request.observacao().isBlank()
                ? null : request.observacao().trim();
        var decisao = new AprovacaoExternaPort.Decisao(request.eventoId(), ordemId,
                request.versaoOrcamento(), request.aprovada(), observacao);
        var anterior = eventos.buscar(request.eventoId());
        if (anterior.isPresent()) {
            if (!anterior.get().equals(decisao)) {
                throw new RegraNegocioException("O evento já foi recebido com outros dados");
            }
            return;
        }
        if (!ordem.getVersaoOrcamento().equals(request.versaoOrcamento())) {
            throw new RegraNegocioException("A decisão se refere a outra versão do orçamento");
        }
        if (request.aprovada()) {
            ordem.aprovarOrcamentoAutomaticamente(observacao);
        } else {
            ordem.recusarOrcamentoAutomaticamente(observacao);
        }
        ordens.save(ordem);
        eventos.registrar(decisao);
        notificacoes.registrarMudanca(ordem);
    }
}
