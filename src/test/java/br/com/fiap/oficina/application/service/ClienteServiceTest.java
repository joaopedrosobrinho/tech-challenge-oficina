package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.cliente.ClienteRequest;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.cliente.Cliente;
import br.com.fiap.oficina.infrastructure.repository.ClienteRepository;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    private ClienteService service;

    @BeforeEach
    void configurar() {
        service = new ClienteService(clienteRepository, ordemServicoRepository);
    }

    @Test
    void deveCadastrarClienteNormalizandoDocumentoEmailETelefone() {
        when(clienteRepository.save(any())).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        var response = service.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.cpfCnpj()).isEqualTo("52998224725");
        assertThat(response.telefone()).isEqualTo("11912345678");
        assertThat(response.email()).isEqualTo("maria@email.com");
        verify(clienteRepository).existsByCpfCnpj("52998224725");
        verify(clienteRepository).existsByEmailIgnoreCase("maria@email.com");
    }

    @Test
    void deveImpedirCadastroComDocumentoDuplicado() {
        when(clienteRepository.existsByCpfCnpj("52998224725"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestValido()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("CPF/CNPJ");
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveBuscarClientePorDocumentoFormatado() {
        when(clienteRepository.findByCpfCnpj("52998224725"))
                .thenReturn(Optional.of(clienteExistente()));

        var response = service.buscarPorCpfCnpj("529.982.247-25");

        assertThat(response.id()).isEqualTo(1L);
        verify(clienteRepository).findByCpfCnpj("52998224725");
    }

    @Test
    void deveAtualizarClienteVerificandoUnicidadeSemConsiderarProprioId() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        var response = service.atualizar(1L, requestValido());

        assertThat(response.nome()).isEqualTo("Maria Silva");
        verify(clienteRepository)
                .existsByCpfCnpjAndIdNot("52998224725", 1L);
        verify(clienteRepository)
                .existsByEmailIgnoreCaseAndIdNot("maria@email.com", 1L);
    }

    @Test
    void deveInformarQuandoClienteNaoExiste() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveImpedirExclusaoDeClienteComOrdemDeServico() {
        Cliente cliente = clienteExistente();
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(ordemServicoRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("ordens de serviço");
        verify(clienteRepository, never()).delete(any());
    }

    private ClienteRequest requestValido() {
        return new ClienteRequest(
                " Maria Silva ",
                "529.982.247-25",
                "(11) 91234-5678",
                " MARIA@EMAIL.COM "
        );
    }

    private Cliente clienteExistente() {
        return Cliente.builder()
                .id(1L)
                .nome("Maria Silva")
                .cpfCnpj("52998224725")
                .telefone("11912345678")
                .email("maria@email.com")
                .build();
    }
}
