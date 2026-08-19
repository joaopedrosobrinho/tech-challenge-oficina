package br.com.fiap.oficina.application.service;

import br.com.fiap.oficina.application.dto.peca.MovimentacaoEstoqueRequest;
import br.com.fiap.oficina.application.dto.peca.PecaRequest;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.exception.RegraNegocioException;
import br.com.fiap.oficina.domain.peca.Peca;
import br.com.fiap.oficina.infrastructure.repository.OrdemServicoRepository;
import br.com.fiap.oficina.infrastructure.repository.PecaRepository;
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
class PecaServiceTest {

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private OrdemServicoRepository ordemServicoRepository;

    private PecaService service;

    @BeforeEach
    void configurar() {
        service = new PecaService(pecaRepository, ordemServicoRepository);
    }

    @Test
    void deveCadastrarPecaNormalizandoCodigo() {
        when(pecaRepository.save(any())).thenAnswer(invocation -> {
            Peca peca = invocation.getArgument(0);
            peca.setId(1L);
            return peca;
        });

        var response = service.cadastrar(requestValido());

        assertThat(response.codigo()).isEqualTo("FLT-001");
        assertThat(response.quantidadeEstoque()).isEqualTo(10);
        assertThat(response.ativo()).isTrue();
        verify(pecaRepository).existsByCodigoIgnoreCase("FLT-001");
    }

    @Test
    void deveImpedirCodigoDuplicado() {
        when(pecaRepository.existsByCodigoIgnoreCase("FLT-001"))
                .thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(requestValido()))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("código");
        verify(pecaRepository, never()).save(any());
    }

    @Test
    void deveAdicionarERetirarEstoque() {
        Peca peca = pecaExistente();
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(peca)).thenReturn(peca);

        assertThat(service.adicionarEstoque(
                1L, new MovimentacaoEstoqueRequest(5)
        ).quantidadeEstoque()).isEqualTo(15);
        assertThat(service.retirarEstoque(
                1L, new MovimentacaoEstoqueRequest(4)
        ).quantidadeEstoque()).isEqualTo(11);
    }

    @Test
    void deveImpedirSaidaComEstoqueInsuficiente() {
        Peca peca = pecaExistente();
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));

        assertThatThrownBy(() -> service.retirarEstoque(
                1L, new MovimentacaoEstoqueRequest(11)
        )).isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Estoque insuficiente");
        verify(pecaRepository, never()).save(any());
    }

    @Test
    void deveAtualizarEAlternarEstadoDaPeca() {
        Peca peca = pecaExistente();
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(pecaRepository.save(peca)).thenReturn(peca);

        assertThat(service.atualizar(1L, requestValido()).codigo())
                .isEqualTo("FLT-001");
        verify(pecaRepository)
                .existsByCodigoIgnoreCaseAndIdNot("FLT-001", 1L);
        assertThat(service.desativar(1L).ativo()).isFalse();
        assertThat(service.ativar(1L).ativo()).isTrue();
    }

    @Test
    void deveBuscarPorCodigoNormalizadoEListarEstoqueBaixo() {
        when(pecaRepository.findByCodigoIgnoreCase("FLT-001"))
                .thenReturn(Optional.of(pecaExistente()));
        when(pecaRepository
                .findByQuantidadeEstoqueLessThanEqualOrderByQuantidadeEstoqueAsc(10))
                .thenReturn(List.of(pecaExistente()));

        assertThat(service.buscarPorCodigo(" flt-001 ").id()).isEqualTo(1L);
        assertThat(service.listarEstoqueBaixo(10)).hasSize(1);
    }

    @Test
    void deveRejeitarLimiteNegativoEInformarPecaInexistente() {
        assertThatThrownBy(() -> service.listarEstoqueBaixo(-1))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("negativo");
        when(pecaRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void deveImpedirExclusaoQuandoVinculadaAOrdem() {
        Peca peca = pecaExistente();
        when(pecaRepository.findById(1L)).thenReturn(Optional.of(peca));
        when(ordemServicoRepository.existsByItensPecaPecaId(1L))
                .thenReturn(true);

        assertThatThrownBy(() -> service.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Desative-a");
        verify(pecaRepository, never()).delete(any());
    }

    private PecaRequest requestValido() {
        return new PecaRequest(
                " flt-001 ",
                " Filtro de óleo ",
                " Filtro do motor ",
                new BigDecimal("45.90"),
                10,
                true
        );
    }

    private Peca pecaExistente() {
        return Peca.builder()
                .id(1L)
                .codigo("FLT-001")
                .nome("Filtro de óleo")
                .descricao("Filtro do motor")
                .valor(new BigDecimal("45.90"))
                .quantidadeEstoque(10)
                .ativo(true)
                .build();
    }
}
