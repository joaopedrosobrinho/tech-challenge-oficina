package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.cliente.ClienteRequest;
import br.com.fiap.oficina.application.dto.cliente.ClienteResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.mapper.ClienteMapper;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.infrastructure.repository.ClienteRepository;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.application.validation.CpfCnpjValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public ClienteResponse cadastrar(ClienteRequest request) {
        String cpfCnpj = somenteNumeros(request.cpfCnpj());
        String email = normalizarEmail(request.email());

        validarCpfCnpj(request.cpfCnpj());
        validarCpfCnpjDuplicado(cpfCnpj);
        validarEmailDuplicado(email);

        Cliente cliente = ClienteMapper.paraEntidade(request);
        Cliente clienteSalvo = clienteRepository.save(cliente);

        return ClienteMapper.paraResponse(clienteSalvo);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll()
                .stream()
                .map(ClienteMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorId(Long id) {
        Cliente cliente = buscarEntidadePorId(id);
        return ClienteMapper.paraResponse(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse buscarPorCpfCnpj(String cpfCnpj) {
        String documentoNormalizado = somenteNumeros(cpfCnpj);

        Cliente cliente = clienteRepository
                .findByCpfCnpj(documentoNormalizado)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado para o CPF/CNPJ informado"
                ));

        return ClienteMapper.paraResponse(cliente);
    }

    @Transactional
    public ClienteResponse atualizar(
            Long id,
            ClienteRequest request
    ) {
        Cliente cliente = buscarEntidadePorId(id);

        String cpfCnpj = somenteNumeros(request.cpfCnpj());
        String email = normalizarEmail(request.email());

        validarCpfCnpj(request.cpfCnpj());
        validarCpfCnpjDuplicadoNaAtualizacao(cpfCnpj, id);
        validarEmailDuplicadoNaAtualizacao(email, id);

        ClienteMapper.atualizarEntidade(cliente, request);

        Cliente clienteAtualizado = clienteRepository.save(cliente);

        return ClienteMapper.paraResponse(clienteAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Cliente cliente = buscarEntidadePorId(id);

        if (ordemServicoRepository.existsByClienteId(id)) {
            throw new RegraNegocioException(
                    "O cliente não pode ser excluído porque possui ordens de serviço"
            );
        }

        clienteRepository.delete(cliente);
    }

    @Transactional(readOnly = true)
    public Cliente buscarEntidadePorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Cliente não encontrado com o ID: " + id
                ));
    }

    private void validarCpfCnpjDuplicado(String cpfCnpj) {
        if (clienteRepository.existsByCpfCnpj(cpfCnpj)) {
            throw new RegraNegocioException(
                    "Já existe um cliente cadastrado com este CPF/CNPJ"
            );
        }
    }

    private void validarCpfCnpj(String cpfCnpj) {
        if (!CpfCnpjValidator.documentoValido(cpfCnpj)) {
            throw new RegraNegocioException(
                    "O CPF/CNPJ informado é inválido"
            );
        }
    }

    private void validarEmailDuplicado(String email) {
        if (clienteRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException(
                    "Já existe um cliente cadastrado com este e-mail"
            );
        }
    }

    private void validarCpfCnpjDuplicadoNaAtualizacao(
            String cpfCnpj,
            Long id
    ) {
        if (clienteRepository.existsByCpfCnpjAndIdNot(cpfCnpj, id)) {
            throw new RegraNegocioException(
                    "Já existe outro cliente cadastrado com este CPF/CNPJ"
            );
        }
    }

    private void validarEmailDuplicadoNaAtualizacao(
            String email,
            Long id
    ) {
        if (clienteRepository.existsByEmailIgnoreCaseAndIdNot(email, id)) {
            throw new RegraNegocioException(
                    "Já existe outro cliente cadastrado com este e-mail"
            );
        }
    }

    private String somenteNumeros(String valor) {
        return valor == null
                ? null
                : valor.replaceAll("\\D", "");
    }

    private String normalizarEmail(String email) {
        return email == null
                ? null
                : email.trim().toLowerCase();
    }
}
