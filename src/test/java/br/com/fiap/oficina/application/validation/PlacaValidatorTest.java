package br.com.fiap.oficina.application.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class PlacaValidatorTest {

    private final PlacaValidator validator = new PlacaValidator();

    @ParameterizedTest
    @ValueSource(strings = {
            "ABC1234",
            "ABC-1234",
            "abc 1234",
            "ABC1D23",
            "abc1d23"
    })
    void deveAceitarPlacasAntigasEMercosul(String placa) {
        assertThat(validator.isValid(placa, null)).isTrue();
        assertThat(PlacaValidator.placaValida(placa)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "ABC123",
            "ABCD123",
            "ABC12D3",
            "1234ABC",
            "ABC-12@34",
            "ABC1DD3"
    })
    void deveRejeitarPlacasInvalidas(String placa) {
        assertThat(validator.isValid(placa, null)).isFalse();
        assertThat(PlacaValidator.placaValida(placa)).isFalse();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void deveDelegarAusenciaDaPlacaParaNotBlank(String placa) {
        assertThat(validator.isValid(placa, null)).isTrue();
        assertThat(PlacaValidator.placaValida(placa)).isFalse();
    }
}
