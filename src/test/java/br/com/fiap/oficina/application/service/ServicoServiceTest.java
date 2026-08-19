package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.servico.ServicoRequest;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.servico.Servico;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.ServicoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    private ServicoService service;

    @BeforeEach
    void configurar() {
        service = new ServicoService(servicoRepository, ordemServicoRepository);
    }

    @Test
    void deveCadastrarServicoNormalizandoCampos() {
        when(servicoRepository.save(any())).thenAnswer(invocation -> {
            Servico servico = invocation.getArgument(0);
            servico.setId(1L);
            return servico;
        });

        var response = service.cadastrar(requestValido());

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Troca de óleo");
        assertThat(response.valor()).isEqualByComparingTo("150.00");
        assertThat(response.ativo()).isTrue();
        verify(servicoRepository).existsByNomeIgnoreCase("Troca de óleo");
    }

    @Test
    void deveImpedirNomeDuplicado() {
        when(servicoRepository.existsByNomeIgnoreCase("Troca de óleo"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestValido()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("nome");
        verify(servicoRepository, never()).save(any());
    }

    @Test
    void deveAtualizarServicoVerificandoOutroRegistro() {
        Servico servico = servicoExistente();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(servico)).thenReturn(servico);

        var response = service.atualizar(1L, requestValido());

        assertThat(response.tempoEstimadoMinutos()).isEqualTo(60);
        verify(servicoRepository)
                .existsByNomeIgnoreCaseAndIdNot("Troca de óleo", 1L);
    }

    @Test
    void deveAtivarEDesativarServico() {
        Servico servico = servicoExistente();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(servico)).thenReturn(servico);

        assertThat(service.desativar(1L).ativo()).isFalse();
        assertThat(service.ativar(1L).ativo()).isTrue();
    }

    @Test
    void devePesquisarNomeSemEspacosExternos() {
        when(servicoRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc("óleo"))
                .thenReturn(List.of(servicoExistente()));

        assertThat(service.pesquisarPorNome("  óleo  ")).hasSize(1);
        verify(servicoRepository)
                .findByNomeContainingIgnoreCaseOrderByNomeAsc("óleo");
    }

    @Test
    void deveInformarServicoInexistente() {
        when(servicoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class)
                .hasMessageContaining("99");
    }

    @Test
    void deveImpedirExclusaoQuandoVinculadoAOrdem() {
        Servico servico = servicoExistente();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(ordemServicoRepository.existsByItensServicoServicoId(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Desative-o");
        verify(servicoRepository, never()).delete(any());
    }

    private ServicoRequest requestValido() {
        return new ServicoRequest(
                " Troca de óleo ",
                " Substituição do óleo ",
                new BigDecimal("150.00"),
                60,
                true
        );
    }

    private Servico servicoExistente() {
        return Servico.builder()
                .id(1L)
                .nome("Troca de óleo")
                .descricao("Substituição do óleo")
                .valor(new BigDecimal("150.00"))
                .tempoEstimadoMinutos(60)
                .ativo(true)
                .build();
    }
}
