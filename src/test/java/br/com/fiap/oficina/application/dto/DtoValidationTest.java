package br.com.fiap.oficina.application.dto;

import br.com.fiap.oficina.application.dto.cliente.ClienteRequest;
import br.com.fiap.oficina.application.dto.ordemservico.AdicionarPecaOrdemRequest;
import br.com.fiap.oficina.application.dto.ordemservico.OrdemServicoRequest;
import br.com.fiap.oficina.application.dto.peca.PecaRequest;
import br.com.fiap.oficina.application.dto.servico.ServicoRequest;
import br.com.fiap.oficina.application.dto.veiculo.VeiculoRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class DtoValidationTest {

    private static final ValidatorFactory VALIDATOR_FACTORY =
            Validation.buildDefaultValidatorFactory();
    private static final Validator VALIDATOR =
            VALIDATOR_FACTORY.getValidator();

    @AfterAll
    static void fecharValidatorFactory() {
        VALIDATOR_FACTORY.close();
    }

    @Test
    void deveAceitarDadosValidosComFormatacao() {
        ClienteRequest cliente = new ClienteRequest(
                "Maria Silva",
                "529.982.247-25",
                "(11) 91234-5678",
                "maria@example.com"
        );
        VeiculoRequest veiculo = new VeiculoRequest(
                "ABC-1234", "Fiat", "Uno", 2020, 1L
        );

        assertThat(VALIDATOR.validate(cliente)).isEmpty();
        assertThat(VALIDATOR.validate(veiculo)).isEmpty();
    }

    @Test
    void deveRejeitarIdsEQuantidadesNaoPositivosInclusiveEmItensInternos() {
        OrdemServicoRequest ordem = new OrdemServicoRequest(
                0L,
                -1L,
                null,
                List.of(new br.com.fiap.oficina.application.dto.ordemservico.ItemServicoRequest(0L, 0)),
                List.of()
        );
        AdicionarPecaOrdemRequest peca =
                new AdicionarPecaOrdemRequest(-1L, 0);

        assertThat(mensagens(VALIDATOR.validate(ordem)))
                .contains(
                        "O ID do cliente deve ser maior que zero",
                        "O ID do veículo deve ser maior que zero",
                        "O ID do serviço deve ser maior que zero",
                        "A quantidade deve ser maior que zero"
                );
        assertThat(mensagens(VALIDATOR.validate(peca)))
                .containsExactlyInAnyOrder(
                        "O ID da peça deve ser maior que zero",
                        "A quantidade deve ser maior que zero"
                );
    }

    @Test
    void deveRejeitarValoresForaDaPrecisaoDoBanco() {
        ServicoRequest servico = new ServicoRequest(
                "Revisão", null, new BigDecimal("12345678901.00"), 60, true
        );
        PecaRequest peca = new PecaRequest(
                "P-1", "Filtro", null, new BigDecimal("10.999"), 1, true
        );

        assertThat(mensagens(VALIDATOR.validate(servico)))
                .contains("O valor deve possuir no máximo 10 dígitos inteiros e 2 decimais");
        assertThat(mensagens(VALIDATOR.validate(peca)))
                .contains("O valor deve possuir no máximo 10 dígitos inteiros e 2 decimais");
    }

    private Set<String> mensagens(Set<? extends ConstraintViolation<?>> violacoes) {
        return violacoes.stream()
                .map(ConstraintViolation::getMessage)
                .collect(java.util.stream.Collectors.toSet());
    }
}
