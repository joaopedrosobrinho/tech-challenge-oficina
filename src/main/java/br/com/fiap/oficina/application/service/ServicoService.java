package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.servico.ServicoRequest;
import br.com.fiap.oficina.application.dto.servico.ServicoResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.mapper.ServicoMapper;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.ServicoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ServicoService {

    private final ServicoRepository servicoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public ServicoResponse cadastrar(ServicoRequest request) {
        String nome = normalizarNome(request.nome());

        validarNomeDuplicado(nome);

        Servico servico = ServicoMapper.paraEntidade(request);
        Servico servicoSalvo = servicoRepository.save(servico);

        return ServicoMapper.paraResponse(servicoSalvo);
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarTodos() {
        return servicoRepository.findAll()
                .stream()
                .map(ServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> listarAtivos() {
        return servicoRepository
                .findAllByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(ServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ServicoResponse> pesquisarPorNome(String nome) {
        String termo = nome == null ? "" : nome.trim();

        return servicoRepository
                .findByNomeContainingIgnoreCaseOrderByNomeAsc(termo)
                .stream()
                .map(ServicoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ServicoResponse buscarPorId(Long id) {
        return ServicoMapper.paraResponse(
                buscarEntidadePorId(id)
        );
    }

    @Transactional
    public ServicoResponse atualizar(
            Long id,
            ServicoRequest request
    ) {
        Servico servico = buscarEntidadePorId(id);
        String nome = normalizarNome(request.nome());

        if (servicoRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new RegraNegocioException(
                    "Já existe outro serviço cadastrado com este nome"
            );
        }

        ServicoMapper.atualizarEntidade(servico, request);

        return ServicoMapper.paraResponse(
                servicoRepository.save(servico)
        );
    }

    @Transactional
    public ServicoResponse ativar(Long id) {
        Servico servico = buscarEntidadePorId(id);
        servico.setAtivo(true);

        return ServicoMapper.paraResponse(
                servicoRepository.save(servico)
        );
    }

    @Transactional
    public ServicoResponse desativar(Long id) {
        Servico servico = buscarEntidadePorId(id);
        servico.setAtivo(false);

        return ServicoMapper.paraResponse(
                servicoRepository.save(servico)
        );
    }

    @Transactional
    public void excluir(Long id) {
        Servico servico = buscarEntidadePorId(id);

        if (ordemServicoRepository.existsByItensServicoServicoId(id)) {
            throw new RegraNegocioException(
                    "O serviço não pode ser excluído porque está vinculado a uma ordem de serviço. Desative-o."
            );
        }

        servicoRepository.delete(servico);
    }

    @Transactional(readOnly = true)
    public Servico buscarEntidadePorId(Long id) {
        return servicoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Serviço não encontrado com o ID: " + id
                ));
    }

    private void validarNomeDuplicado(String nome) {
        if (servicoRepository.existsByNomeIgnoreCase(nome)) {
            throw new RegraNegocioException(
                    "Já existe um serviço cadastrado com este nome"
            );
        }
    }

    private String normalizarNome(String nome) {
        return nome == null ? null : nome.trim();
    }
}