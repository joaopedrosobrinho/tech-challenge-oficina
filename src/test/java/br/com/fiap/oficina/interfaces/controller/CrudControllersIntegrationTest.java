package br.com.fiap.oficina.interfaces.controller;

import br.com.fiap.oficina.application.dto.cliente.ClienteResponse;
import br.com.fiap.oficina.application.dto.ordemservico.AprovacaoOrcamentoResponse;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoResponse;
import br.com.fiap.oficina.application.dto.peca.PecaResponse;
import br.com.fiap.oficina.application.dto.servico.ServicoResponse;
import br.com.fiap.oficina.application.dto.veiculo.VeiculoResponse;
import br.com.fiap.oficina.application.exception.RecursoNaoEncontradoException;
import br.com.fiap.oficina.application.service.ClienteService;
import br.com.fiap.oficina.application.service.OrdemServicoService;
import br.com.fiap.oficina.application.service.PecaService;
import br.com.fiap.oficina.application.service.ServicoService;
import br.com.fiap.oficina.application.service.VeiculoService;
import br.com.fiap.oficina.infrastructure.repository.UsuarioRepository;
import br.com.fiap.oficina.infrastructure.security.JwtService;
import br.com.fiap.oficina.domain.ordemservico.StatusAprovacaoOrcamento;
import br.com.fiap.oficina.domain.ordemservico.StatusOrdemServico;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({
        ClienteController.class,
        VeiculoController.class,
        ServicoController.class,
        PecaController.class,
        OrdemServicoController.class
})
@AutoConfigureMockMvc(addFilters = false)
class CrudControllersIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;
    @MockitoBean
    private VeiculoService veiculoService;
    @MockitoBean
    private ServicoService servicoService;
    @MockitoBean
    private PecaService pecaService;
    @MockitoBean
    private OrdemServicoService ordemServicoService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UsuarioRepository usuarioRepository;

    @Test
    void deveCadastrarClienteRetornando201ELocation() throws Exception {
        when(clienteService.cadastrar(any())).thenReturn(
                new ClienteResponse(
                        1L, "Maria", "52998224725",
                        "11912345678", "maria@email.com"
                )
        );

        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Maria",
                                  "cpfCnpj": "529.982.247-25",
                                  "telefone": "(11) 91234-5678",
                                  "email": "maria@email.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/clientes/1"))
                .andExpect(jsonPath("$.cpfCnpj").value("52998224725"));
    }

    @Test
    void deveRetornar400SemChamarServiceParaClienteInvalido() throws Exception {
        mockMvc.perform(post("/api/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","cpfCnpj":"123","telefone":"1","email":"x"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.nome").exists())
                .andExpect(jsonPath("$.campos.cpfCnpj").exists());
        verify(clienteService, never()).cadastrar(any());
    }

    @Test
    void deveCadastrarVeiculoCom201() throws Exception {
        when(veiculoService.cadastrar(any())).thenReturn(
                new VeiculoResponse(2L, "ABC1D23", "Honda", "Civic", 2022, 1L, "Maria")
        );

        mockMvc.perform(post("/api/veiculos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"placa":"ABC1D23","marca":"Honda","modelo":"Civic","ano":2022,"clienteId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/veiculos/2"));
    }

    @Test
    void deveCadastrarServicoCom201() throws Exception {
        when(servicoService.cadastrar(any())).thenReturn(
                new ServicoResponse(3L, "Revisão", null, new BigDecimal("150.00"), 60, true)
        );

        mockMvc.perform(post("/api/servicos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Revisão","valor":150.00,"tempoEstimadoMinutos":60,"ativo":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/servicos/3"));
    }

    @Test
    void deveCadastrarEMovimentarEstoqueDaPeca() throws Exception {
        PecaResponse peca = new PecaResponse(
                4L, "FLT-001", "Filtro", null,
                new BigDecimal("45.90"), 10, true
        );
        when(pecaService.cadastrar(any())).thenReturn(peca);
        when(pecaService.adicionarEstoque(eq(4L), any())).thenReturn(peca);

        mockMvc.perform(post("/api/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"codigo":"FLT-001","nome":"Filtro","valor":45.90,"quantidadeEstoque":5,"ativo":true}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/pecas/4"));

        mockMvc.perform(patch("/api/pecas/4/entrada-estoque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantidade\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));
    }

    @Test
    void deveCriarOrdemEExecutarComandoDeAprovacao() throws Exception {
        OrdemServicoResponse ordem = ordemResponse();
        when(ordemServicoService.criar(any())).thenReturn(ordem);
        when(ordemServicoService.aprovarOrcamento(eq(5L), any()))
                .thenReturn(new AprovacaoOrcamentoResponse(
                        5L, "OS-123", StatusOrdemServico.EM_EXECUCAO,
                        StatusAprovacaoOrcamento.APROVADO,
                        new BigDecimal("150.00"), null, "Aprovado"
                ));

        mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1,"veiculoId":2,"servicos":[{"servicoId":3,"quantidade":1}],"pecas":[]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/ordens-servico/5"));

        mockMvc.perform(post("/api/ordens-servico/5/orcamento/aprovar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"observacao\":\"Aprovado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusOrdemServico")
                        .value("EM_EXECUCAO"));
    }

    @Test
    void deveValidarQuantidadeAoAdicionarPecaNaOrdem() throws Exception {
        mockMvc.perform(post("/api/ordens-servico/5/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pecaId\":4,\"quantidade\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos.quantidade").exists());
        verify(ordemServicoService, never()).adicionarPeca(any(), any());
    }

    @Test
    void deveTraduzirRecursoInexistentePara404() throws Exception {
        when(servicoService.buscarPorId(99L))
                .thenThrow(new RecursoNaoEncontradoException("Serviço não encontrado"));

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/servicos/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensagem").value("Serviço não encontrado"));
    }

    @Test
    void deveExcluirClienteCom204() throws Exception {
        mockMvc.perform(delete("/api/clientes/1"))
                .andExpect(status().isNoContent());
        verify(clienteService).excluir(1L);
    }

    private OrdemServicoResponse ordemResponse() {
        return new OrdemServicoResponse(
                5L, "OS-123",
                1L, "Maria", "52998224725",
                2L, "ABC1D23", "Honda", "Civic",
                StatusOrdemServico.EM_DIAGNOSTICO,
                new BigDecimal("150.00"), null,
                null, null, null, null, null, null, null,
                false, null, false, null,
                List.of(), List.of()
        );
    }
}
