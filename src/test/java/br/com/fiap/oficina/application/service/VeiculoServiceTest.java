package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.veiculo.VeiculoRequest;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.domain.veiculo.Veiculo;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.VeiculoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VeiculoServiceTest {

    @Mock
    private VeiculoRepository veiculoRepository;

    @Mock
    private ClienteService clienteService;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    private VeiculoService service;

    @BeforeEach
    void configurar() {
        service = new VeiculoService(
                veiculoRepository, clienteService, ordemServicoRepository
        );
    }

    @Test
    void deveCadastrarVeiculoNormalizandoPlacaEVinculandoCliente() {
        Cliente cliente = clienteExistente();
        when(clienteService.buscarEntidadePorId(1L)).thenReturn(cliente);
        when(veiculoRepository.save(any())).thenAnswer(invocation -> {
            Veiculo veiculo = invocation.getArgument(0);
            veiculo.setId(10L);
            return veiculo;
        });

        var response = service.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.placa()).isEqualTo("ABC1234");
        assertThat(response.clienteId()).isEqualTo(1L);
        verify(veiculoRepository).existsByPlacaIgnoreCase("ABC1234");
    }

    @Test
    void deveImpedirCadastroComPlacaDuplicadaMesmoComMascara() {
        when(veiculoRepository.existsByPlacaIgnoreCase("ABC1234"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestValido()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("placa");
        verify(clienteService, never()).buscarEntidadePorId(any());
    }

    @Test
    void deveBuscarPorPlacaNormalizada() {
        when(veiculoRepository.findByPlacaIgnoreCase("ABC1234"))
                .thenReturn(Optional.of(veiculoExistente()));

        var response = service.buscarPorPlaca("abc-1234");

        assertThat(response.id()).isEqualTo(10L);
        verify(veiculoRepository).findByPlacaIgnoreCase("ABC1234");
    }

    @Test
    void deveAtualizarVeiculoSemConflitarComProprioRegistro() {
        Veiculo veiculo = veiculoExistente();
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(clienteService.buscarEntidadePorId(1L))
                .thenReturn(clienteExistente());
        when(veiculoRepository.save(veiculo)).thenReturn(veiculo);

        var response = service.atualizar(10L, requestValido());

        assertThat(response.placa()).isEqualTo("ABC1234");
        verify(veiculoRepository)
                .existsByPlacaIgnoreCaseAndIdNot("ABC1234", 10L);
    }

    @Test
    void deveListarSomenteVeiculosDoClienteExistente() {
        when(clienteService.buscarEntidadePorId(1L))
                .thenReturn(clienteExistente());
        when(veiculoRepository.findAllByClienteId(1L))
                .thenReturn(List.of(veiculoExistente()));

        var responses = service.listarPorCliente(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().placa()).isEqualTo("ABC1234");
    }

    @Test
    void deveInformarQuandoVeiculoNaoExiste() {
        when(veiculoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveImpedirExclusaoDeVeiculoComOrdemDeServico() {
        Veiculo veiculo = veiculoExistente();
        when(veiculoRepository.findById(10L)).thenReturn(Optional.of(veiculo));
        when(ordemServicoRepository.existsByVeiculoId(10L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(10L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ordens de serviço");
        verify(veiculoRepository, never()).delete(any());
    }

    private VeiculoRequest requestValido() {
        return new VeiculoRequest("abc-1234", " Fiat ", " Uno ", 2020, 1L);
    }

    private Cliente clienteExistente() {
        return Cliente.builder().id(1L).nome("Maria Silva").build();
    }

    private Veiculo veiculoExistente() {
        return Veiculo.builder()
                .id(10L)
                .placa("ABC1234")
                .marca("Fiat")
                .modelo("Uno")
                .ano(2020)
                .cliente(clienteExistente())
                .build();
    }
}
