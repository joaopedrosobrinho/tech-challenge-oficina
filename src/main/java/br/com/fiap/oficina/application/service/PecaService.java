package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.peca.MovimentacaoEstoqueRequest;
import br.com.fiap.oficina.application.dto.peca.PecaRequest;
import br.com.fiap.oficina.application.dto.peca.PecaResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.mapper.PecaMapper;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.PecaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PecaService {

    private final PecaRepository pecaRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public PecaResponse cadastrar(PecaRequest request) {
        String codigo = normalizarCodigo(request.codigo());

        validarCodigoDuplicado(codigo);

        Peca peca = PecaMapper.paraEntidade(request);
        Peca pecaSalva = pecaRepository.save(peca);

        return PecaMapper.paraResponse(pecaSalva);
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> listarTodas() {
        return pecaRepository.findAll()
                .stream()
                .map(PecaMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> listarAtivas() {
        return pecaRepository
                .findAllByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(PecaMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PecaResponse buscarPorId(Long id) {
        return PecaMapper.paraResponse(
                buscarEntidadePorId(id)
        );
    }

    @Transactional(readOnly = true)
    public PecaResponse buscarPorCodigo(String codigo) {
        String codigoNormalizado = normalizarCodigo(codigo);

        Peca peca = pecaRepository
                .findByCodigoIgnoreCase(codigoNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Peça não encontrada com o código: "
                                + codigoNormalizado
                ));

        return PecaMapper.paraResponse(peca);
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> pesquisarPorNome(String nome) {
        String termo = nome == null ? "" : nome.trim();

        return pecaRepository
                .findByNomeContainingIgnoreCaseOrderByNomeAsc(termo)
                .stream()
                .map(PecaMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PecaResponse> listarEstoqueBaixo(Integer limite) {
        if (limite == null || limite < 0) {
            throw new RegraNegocioException(
                    "O limite de estoque não pode ser negativo"
            );
        }

        return pecaRepository
                .findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(
                        limite
                )
                .stream()
                .map(PecaMapper::paraResponse)
                .toList();
    }

    @Transactional
    public PecaResponse atualizar(
            Long id,
            PecaRequest request
    ) {
        Peca peca = buscarEntidadePorId(id);
        String codigo = normalizarCodigo(request.codigo());

        if (pecaRepository.existsByCodigoIgnoreCaseAndIdNot(codigo, id)) {
            throw new RegraNegocioException(
                    "Já existe outra peça cadastrada com este código"
            );
        }

        PecaMapper.atualizarEntidade(peca, request);

        return PecaMapper.paraResponse(
                pecaRepository.save(peca)
        );
    }

    @Transactional
    public PecaResponse adicionarEstoque(
            Long id,
            MovimentacaoEstoqueRequest request
    ) {
        Peca peca = buscarEntidadePorId(id);

        peca.devolverEstoque(request.quantidade());

        return PecaMapper.paraResponse(
                pecaRepository.save(peca)
        );
    }

    @Transactional
    public PecaResponse retirarEstoque(
            Long id,
            MovimentacaoEstoqueRequest request
    ) {
        Peca peca = buscarEntidadePorId(id);

        try {
            peca.baixarEstoque(request.quantidade());
        } catch (IllegalArgumentException exception) {
            throw new RegraNegocioException(exception.getMessage());
        }

        return PecaMapper.paraResponse(
                pecaRepository.save(peca)
        );
    }

    @Transactional
    public PecaResponse ativar(Long id) {
        Peca peca = buscarEntidadePorId(id);
        peca.setAtivo(true);

        return PecaMapper.paraResponse(
                pecaRepository.save(peca)
        );
    }

    @Transactional
    public PecaResponse desativar(Long id) {
        Peca peca = buscarEntidadePorId(id);
        peca.setAtivo(false);

        return PecaMapper.paraResponse(
                pecaRepository.save(peca)
        );
    }

    @Transactional
    public void excluir(Long id) {
        Peca peca = buscarEntidadePorId(id);

        if (ordemServicoRepository.existsByItensPecaPecaId(id)) {
            throw new RegraNegocioException(
                    "A peça não pode ser excluída porque está vinculada a uma ordem de serviço. Desative-a."
            );
        }

        pecaRepository.delete(peca);
    }

    @Transactional(readOnly = true)
    public Peca buscarEntidadePorId(Long id) {
        return pecaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Peça não encontrada com o ID: " + id
                ));
    }

    private void validarCodigoDuplicado(String codigo) {
        if (pecaRepository.existsByCodigoIgnoreCase(codigo)) {
            throw new RegraNegocioException(
                    "Já existe uma peça cadastrada com este código"
            );
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null
                ? null
                : codigo.trim().toUpperCase();
    }
}