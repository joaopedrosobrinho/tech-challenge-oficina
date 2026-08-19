package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.veiculo.VeiculoRequest;
import br.com.fiap.oficina.application.dto.veiculo.VeiculoResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.application.mapper.VeiculoMapper;
import br.com.fiap.oficina.application.validation.PlacaValidator;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final ClienteService clienteService;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public VeiculoResponse cadastrar(VeiculoRequest request) {
        validarPlaca(request.placa());
        String placa = normalizarPlaca(request.placa());

        validarPlacaDuplicada(placa);

        Cliente cliente = clienteService.buscarEntidadePorId(
                request.clienteId()
        );

        Veiculo veiculo = VeiculoMapper.paraEntidade(
                request,
                cliente
        );

        Veiculo veiculoSalvo = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraResponse(veiculoSalvo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarTodos() {
        return veiculoRepository.findAll()
                .stream()
                .map(VeiculoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorId(Long id) {
        return VeiculoMapper.paraResponse(
                buscarEntidadePorId(id)
        );
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscarPorPlaca(String placa) {
        String placaNormalizada = normalizarPlaca(placa);

        Veiculo veiculo = veiculoRepository
                .findByPlacaIgnoreCase(placaNormalizada)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Veículo não encontrado para a placa: "
                                + placaNormalizada
                ));

        return VeiculoMapper.paraResponse(veiculo);
    }

    @Transactional(readOnly = true)
    public List<VeiculoResponse> listarPorCliente(Long clienteId) {
        clienteService.buscarEntidadePorId(clienteId);

        return veiculoRepository.findAllByClienteId(clienteId)
                .stream()
                .map(VeiculoMapper::paraResponse)
                .toList();
    }

    @Transactional
    public VeiculoResponse atualizar(
            Long id,
            VeiculoRequest request
    ) {
        Veiculo veiculo = buscarEntidadePorId(id);
        validarPlaca(request.placa());
        String placa = normalizarPlaca(request.placa());

        validarPlacaDuplicadaNaAtualizacao(placa, id);

        Cliente cliente = clienteService.buscarEntidadePorId(
                request.clienteId()
        );

        VeiculoMapper.atualizarEntidade(
                veiculo,
                request,
                cliente
        );

        Veiculo veiculoAtualizado = veiculoRepository.save(veiculo);

        return VeiculoMapper.paraResponse(veiculoAtualizado);
    }

    @Transactional
    public void excluir(Long id) {
        Veiculo veiculo = buscarEntidadePorId(id);

        if (ordemServicoRepository.existsByVeiculoId(id)) {
            throw new RegraNegocioException(
                    "O veículo não pode ser excluído porque possui ordens de serviço"
            );
        }

        veiculoRepository.delete(veiculo);
    }

    @Transactional(readOnly = true)
    public Veiculo buscarEntidadePorId(Long id) {
        return veiculoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Veículo não encontrado com o ID: " + id
                ));
    }

    private void validarPlacaDuplicada(String placa) {
        if (veiculoRepository.existsByPlacaIgnoreCase(placa)) {
            throw new RegraNegocioException(
                    "Já existe um veículo cadastrado com esta placa"
            );
        }
    }

    private void validarPlacaDuplicadaNaAtualizacao(
            String placa,
            Long id
    ) {
        if (veiculoRepository
                .existsByPlacaIgnoreCaseAndIdNot(placa, id)) {

            throw new RegraNegocioException(
                    "Já existe outro veículo cadastrado com esta placa"
            );
        }
    }

    private String normalizarPlaca(String placa) {
        return PlacaValidator.normalizar(placa);
    }

    private void validarPlaca(String placa) {
        if (!PlacaValidator.placaValida(placa)) {
            throw new RegraNegocioException(
                    "A placa deve estar no formato antigo ou Mercosul"
            );
        }
    }
}
